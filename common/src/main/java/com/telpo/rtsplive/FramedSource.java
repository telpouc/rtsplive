package com.telpo.rtsplive;

import java.nio.ByteBuffer;

/**
 * 帧源接口
 *
 * @author  gfm
 * @since   1.0
 */
public interface FramedSource {
    /**
     * 输出帧监听接口
     */
    interface OutputListener {
        /**
         * 输出帧回调方法
         *
         * @param buffer 帧数据缓冲区
         * @param size 帧数据大小
         * @param presentationTimeUs 帧时间戳，单位：微秒
         */
        void onFrameOutput(ByteBuffer buffer, int size, long presentationTimeUs);
    }

    /**
     * 启动输出帧
     */
    void start();

    /**
     * 停止输出帧
     */
    void stop();

    /**
     * 设置输出帧监听
     *
     * @see OutputListener
     */
    void setOutputListener(OutputListener listener);
}
