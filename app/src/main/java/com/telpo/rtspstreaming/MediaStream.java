package com.telpo.rtspstreaming;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.telpo.rtsplive.MediaSubsessionDelegate;
import com.telpo.rtsplive.RtspServer;
import com.telpo.rtsplive.VideoEncoder;

import java.util.List;

/**
 * MediaStream
 * @author gfm
 */
public class MediaStream implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private int mVideoQuality = CamcorderProfile.QUALITY_480P;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;  // TODO：适配设备

    private final VideoEncoder mVideoEncoder;
    private final MediaSubsessionDelegate mVideoDelegate;
    private final RtspServer mRtspServer;
    private MutableLiveData<String> mRtspUrl = new MutableLiveData<>();

    public MediaStream() {
        mVideoEncoder = VideoEncoder.createDefaultVideoEncoder();  // 创建默认视频编码器 (MediaCodec NV21 -> H264)
        mVideoDelegate = MediaSubsessionDelegate.createDefaultVideoDelegate(mVideoEncoder); // 创建H264媒体会话代理

        mRtspServer = RtspServer.createDefaultBuilder()
                // .setStreamName("")     // 如果设置了streamName，rtsp url 后面将加上 /streamName
                // .setUserName("admin")  // 设置鉴权用户名
                // .setPassword("123456") // 设置鉴权密码
                .setVideoDelegate(mVideoDelegate)
                .setListener(server -> {
                    if (server.getErrno() != 0) {
                        Log.e("RtspServer", server.getResultMsg());
                    } else {
                        String rtspUrl = server.getRtspUrl();
                        Log.d("RtspServer","rtspUrl: " + rtspUrl);
                        mRtspUrl.postValue(rtspUrl);
                    }
                }).build();

        startRtspServer();
    }

    public void startRtspServer() {
        mRtspServer.startService();
    }

    public void stopRtspServer() {
        mRtspServer.stopService();
    }

    public LiveData<String> getRtspUrl() {
        return mRtspUrl;
    }

    public void setVideoQuality(int videoQuality) {
        this.mVideoQuality = videoQuality;
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        CameraThread.getHandler().post(() -> {
            if (mCamera != null) {
                mVideoEncoder.stop();
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallbackWithBuffer(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                releaseCamera();
            }
        });
    }


    /**
     * 开启预览
     */
    public void startPreview() {
        CameraThread.getHandler().post(() -> {
            if (mCamera == null) {
                openCamera();
            }
            if (mCamera != null) {
                try {
                    mCamera.startPreview();
                    // mCamera.autoFocus(null);

                    int previewFormat = mCamera.getParameters().getPreviewFormat();
                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    int size = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8;
                    mCamera.addCallbackBuffer(new byte[size]);
                    mCamera.setPreviewCallbackWithBuffer(previewCallback);
                    if (mVideoDelegate.isStreamRunning()) {
                        mVideoEncoder.start();
                    }
                    if (mSurfaceHolder != null) {
                        mCamera.setPreviewDisplay(mSurfaceHolder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openCamera() {
        try {
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            CamcorderProfile camcorderProfile = getCamcorderProfile(mVideoQuality);
            Camera.Size previewSize = getSizeWithClosestRatio(previewSizes,
                    camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);

            Log.d("openCamera", "previewSize: " + previewSize.width + "x" + previewSize.height + " rotation: " + camInfo.orientation);

            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(90); // TODO：适配设备

            mVideoEncoder.initResolution(previewSize.width, previewSize.height, camInfo.orientation);
        } catch (Exception e) {
            releaseCamera();
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if(data == null || camera == null) {
                return;
            }
//            int previewFormat = mCamera.getParameters().getPreviewFormat();
//            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
//            int size = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8;
//            if (data.length != size) {
//                camera.addCallbackBuffer(data);
//                return;
//            }
            if (!mVideoEncoder.queueInputFrame(data, camera)) {
                camera.addCallbackBuffer(data);
            }
        }
    };

    private void setPreviewDisplay(SurfaceHolder holder) {
        CameraThread.getHandler().post(() -> {
            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (mCamera != null) {
            setPreviewDisplay(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private CamcorderProfile getCamcorderProfile(int videoQuality) {
        CamcorderProfile camcorderProfile = null;
        switch (videoQuality) {
            case CamcorderProfile.QUALITY_QVGA:
                if (CamcorderProfile.hasProfile(mCameraId, videoQuality)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, videoQuality);
                } else {
                    return getCamcorderProfile(CamcorderProfile.QUALITY_LOW);
                }
                break;

            case CamcorderProfile.QUALITY_480P:
                if (CamcorderProfile.hasProfile(mCameraId, videoQuality)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, videoQuality);
                } else {
                    return getCamcorderProfile(CamcorderProfile.QUALITY_QVGA);
                }
                break;

            case CamcorderProfile.QUALITY_720P:
                if (CamcorderProfile.hasProfile(mCameraId, videoQuality)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, videoQuality);
                } else {
                    return getCamcorderProfile(CamcorderProfile.QUALITY_480P);
                }
                break;

            case CamcorderProfile.QUALITY_1080P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_1080P);
                } else {
                    return getCamcorderProfile(CamcorderProfile.QUALITY_720P);
                }
                break;

            case CamcorderProfile.QUALITY_2160P:
                try {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_2160P);
                } catch (Exception e) {
                    return getCamcorderProfile(CamcorderProfile.QUALITY_HIGH);
                }
                break;

            case CamcorderProfile.QUALITY_HIGH:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);
                break;

            case CamcorderProfile.QUALITY_LOW:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
                break;
        }

        return camcorderProfile;
    }

    public static Camera.Size getSizeWithClosestRatio(List<Camera.Size> sizes, int width, int height) {
        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            if (size.width == width && size.height == height)
                return size;

            double ratio = (double) size.height / size.width;

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) MIN_TOLERANCE = ratio;
            else continue;

            if (Math.abs(size.height - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - height);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
        return optimalSize;
    }

}
