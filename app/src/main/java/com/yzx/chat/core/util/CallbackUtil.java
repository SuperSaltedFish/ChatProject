package com.yzx.chat.core.util;

import com.yzx.chat.core.listener.ResultCallback;

/**
 * Created by YZX on 2019年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CallbackUtil {
    public static <T> void callResult(T result, ResultCallback<T> callback) {
        if (callback != null) {
            callback.onResult(result);
        }
    }

    public static void callFailure(int code, String error, ResultCallback callback) {
        if (callback != null) {
            callback.onFailure(code, error);
        }
    }
}
