package com.telpo.rtsplive;

import android.hardware.Camera;

/**
 * 视频编码器接口
 *
 * @author  gfm
 * @since   1.0
 */
public interface VideoEncoder extends FramedSource {
    /**
     * 获取媒体类型，例如: video/avc
     */
    String getMime();

    /**
     * 获取视频码率
     */
    int getBitrate();

    /**
     * 设置源视频帧的宽度、高度以及需要旋转的角度
     *
     * @param width 源视频帧宽度
     * @param height 源视频帧高度
     * @param rotation 需要旋转的角度，可以为：0、90、180、270
     */
    void initResolution(int width, int height, int rotation);

    /**
     * 把输入帧放到编码队列
     * <p>
     * 需要调用 {@link #start()} 方法启动编码器后调用 {@code queueInputFrame} 方法才有效
     *
     * @param data 源视频帧数据
     * @param camera Camera 对象，如果不为 null，Camera 初始化时需要使用 addCallbackBuffer + setPreviewCallbackWithBuffer 方法处理
     * @return 如果返回 true，表示 queueInputFrame 方法内部已调用 addCallbackBuffer，所以你应该这样处理：
     * <pre> {@code
     * if (!mVideoEncoder.queueInputFrame(data, camera)) {
     *     camera.addCallbackBuffer(data);
     * }}</pre>
     */
    boolean queueInputFrame(byte[] data, Camera camera);

    /**
     * 创建默认视频编码器
     * <p>
     * 默认实现类是
     * com.telpo.rtsplive.HWVideoEncoder，通过反射来创建对象
     * <p>
     * 默认实现使用 {@link android.media.MediaCodec} 编码，媒体类型是 video/avc，视频码率自动根据分辨率计算
     *
     * @return 返回新创建的 VideoEncoder 对象
     * @throws RuntimeException 如果反射调用异常
     */
    static VideoEncoder createDefaultVideoEncoder() {
        try {
            return (VideoEncoder) Class.forName("com.telpo.rtsplive.HWVideoEncoder").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
