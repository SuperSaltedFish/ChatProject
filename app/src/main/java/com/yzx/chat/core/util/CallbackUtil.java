package com.yzx.chat.core.util;

import android.os.Handler;
import android.os.Looper;

import com.yzx.chat.core.listener.ResultCallback;

/**
 * Created by YZX on 2019年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CallbackUtil {
    private static Handler sUIHandler = new Handler(Looper.getMainLooper());

    public static <T> void callResult(final T result, final ResultCallback<T> callback) {
        if (callback != null) {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(result);
                    }
                });
            } else {
                callback.onResult(result);
            }
        }
    }

    public static void callFailure(final int code, final String error, final ResultCallback callback) {
        if (callback != null) {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(code, error);
                    }
                });
            } else {
                callback.onFailure(code, error);
            }
        }
    }
}
