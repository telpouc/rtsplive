package com.telpo.rtsplive;

import java.lang.reflect.Constructor;

/**
 * 媒体子会话委托接口
 *
 * @author  gfm
 * @since   1.0
 */
public interface MediaSubsessionDelegate {
    /**
     * 创建 native 媒体子会话
     * <p>
     * 这个方法只在 RtspServer 内部实现调用，请不要在其它地方调用。
     */
    long createMediaSubsession(long nativeService);

    /**
     * 当 Rtsp 流创建时被调用
     * <p>
     * 这个方法只在 MediaSubsessionDelegate 内部实现调用，请不要在其它地方调用。
     */
    void onStreamCreate();

    /**
     * 当 Rtsp 流关闭时被调用
     * <p>
     * 这个方法只在 MediaSubsessionDelegate 内部实现调用，请不要在其它地方调用。
     */
    void onStreamClose();

    /**
     * 判断 Rtsp 流是否已创建
     *
     * @return 当 {@link #onStreamCreate} 被调用后返回 true， 当 {@link #onStreamClose} 被调用后返回 false
     * @see #onStreamCreate
     * @see #onStreamClose
     */
    boolean isStreamRunning();

    /**
     * 创建默认视频编码器
     * <p>
     * 默认实现类是
     * com.telpo.rtsplive.H264SubsessionDelegate，通过反射来创建对象
     * <p>
     * 默认实现是对 H264 视频流的处理，提供 Rtsp 协议所需数据
     *
     * @param framedSource 提供 H246 数据帧
     * @return 返回新创建的 MediaSubsessionDelegate 对象
     * @throws RuntimeException 如果反射调用异常
     */
    static MediaSubsessionDelegate createDefaultVideoDelegate(FramedSource framedSource) {
        try {
            Class<?> clazz = Class.forName("com.telpo.rtsplive.H264SubsessionDelegate");
            Constructor c = clazz.getConstructor(FramedSource.class);
            return (MediaSubsessionDelegate) c.newInstance(framedSource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
