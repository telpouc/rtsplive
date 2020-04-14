package com.telpo.rtspstreaming;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

/**
 * CameraThread
 * @author gfm
 */
public class CameraThread extends HandlerThread {
    private static @NonNull Handler handler;
    private static @NonNull CameraThread instance;

    private CameraThread(String name) {
        super(name);
    }

    static {
        instance = new CameraThread("CameraThread");
        instance.setDaemon(true);
        instance.start();
        handler = new Handler(instance.getLooper());
    }

    @NonNull
    public static Handler getHandler() {
        return handler;
    }

    @NonNull
    public static CameraThread getInstance() {
        return instance;
    }
}
