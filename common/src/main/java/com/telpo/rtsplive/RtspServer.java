package com.telpo.rtsplive;

import java.util.concurrent.Executor;

/**
 * {@code RtspServer} 接口
 *
 * @author  gfm
 * @since   1.0
 */
public interface RtspServer {

    /**
     * RtspServer 监听接口
     * <p>
     * 在调用 {@link #startService} 后会异步启动 Rtsp 服务，可以设置监听接口，然后判断启动成功与否
     *
     * @see Builder#setListener(Listener)
     */
    interface Listener {
        /**
         * {@code onStart} 方法在 Rtsp 服务启动完成后回调。<p>这时可以调用
         * {@link #getErrno} 获取错误码，调用 {@link #getResultMsg} 获取结果消息。<p>
         * 如果启动成功，错误码为 0，可以调用 {@link #getRtspUrl} 获取 Rtsp Url 地址。
         *
         * @param server RtspServer 实例对象
         */
        void onStart(RtspServer server);
    }

    /**
     * 启动 Rtsp 服务
     *
     * @see Listener#onStart
     * @see #stopService
     */
    void startService();

    /**
     * 停止 Rtsp 服务
     *
     * @see #startService
     */
    void stopService();

    /**
     * 获取 Rtsp Url 地址
     *
     * @return 如果启动成功，返回 Rtsp Url 地址，否则返回空字符串
     *
     * @see Listener#onStart
     */
    String getRtspUrl();

    /**
     * 获取错误码
     *
     * @see Listener#onStart
     */
    int getErrno();

    /**
     * 获取结果消息
     * <p>一般用于调试，在 {@link #getErrno} 返回非 0 时获取错误描述
     *
     * @see Listener#onStart
     */
    String getResultMsg();

    /**
     * 创建默认 Builder（RtspServer 对象使用 Builder 模式来创建）
     * <p>
     * 默认实现类是
     * com.telpo.rtsplive.RtspServerBuilder，通过反射来创建对象
     *
     * @return 返回新创建的 RtspServerBuilder 对象
     * @throws RuntimeException 如果反射调用异常
     * @see Builder
     */
    static Builder createDefaultBuilder() {
        try {
            return (Builder) Class.forName("com.telpo.rtsplive.RtspServerBuilder").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Builder 基础抽象类
     * <p>
     * 默认实现目前仅支持视频流，不支持音频流
     * <p>
     * 如果需要自己实现 RtspServer，可以继承 {@code Builder} 并实现 {@link #build}  方法
     */
    abstract class Builder {
        protected String streamName;
        protected short port = 8554;
        protected String userName;
        protected String password;
        protected Listener listener;
        protected Executor executor;
        protected MediaSubsessionDelegate videoDelegate;

        /**
         * 设置 Rtsp 流名称
         *
         * @param streamName Rtsp 流名称，不设置默认为空字符串。<p>Rtsp Url 地址后会带上 /streamName
         * @return This Builder
         */
        public Builder setStreamName(String streamName) {
            this.streamName = streamName;
            return this;
        }

        /**
         * 设置 Rtsp 端口
         *
         * @param port Rtsp 监听端口，不设置默认为 8554
         * @return This Builder
         */
        public Builder setPort(short port) {
            this.port = port;
            return this;
        }

        /**
         * 设置 Rtsp 鉴权用户名
         *
         * @param username Rtsp 鉴权用户名，不设置默认为 null（不鉴权）
         * @return This Builder
         */
        public Builder setUserName(String username) {
            this.userName = username;
            return this;
        }

        /**
         * 设置 Rtsp 鉴权密码
         *
         * @param password Rtsp 鉴权密码，不设置默认为 null（不鉴权）
         * @return This Builder
         */
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * 设置 Executor，用于设置 Rtsp 服务要运行在哪个线程
         * <p>
         * 默认实现在 Rtsp 服务启动后，会一直阻塞 Executor 线程直到服务停止
         * <p>
         * 如果不设置，默认实现会在 {@link #build} 方法创建默认的 Executor
         *
         * @param executor 要使用的 Executor
         * @return This Builder
         */
        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * 设置 Listener，用于监听 Rtsp 服务启动完成
         *
         * @param listener {@code Listener} 实例对象
         * @return This Builder
         */
        public Builder setListener(Listener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 设置视频会话委托
         *
         * @param videoDelegate {@code MediaSubsessionDelegate} 实例对象，不能为 null
         * @return This Builder
         * @see MediaSubsessionDelegate
         */
        public Builder setVideoDelegate(MediaSubsessionDelegate videoDelegate) {
            this.videoDelegate = videoDelegate;
            return this;
        }

        /**
         * 使用这个 Builder 提供的参数来创建 {@link RtspServer}
         * <p>
         * 调用这个方法不会启动服务，要启动服务还需要调用 {@link #startService()}
         */
        public abstract RtspServer build();
    }
}
