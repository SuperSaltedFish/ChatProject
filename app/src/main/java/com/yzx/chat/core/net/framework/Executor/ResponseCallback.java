package com.yzx.chat.core.net.framework.Executor;


import androidx.annotation.Nullable;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface ResponseCallback {
    void onResponse(int responseCode, @Nullable byte[] body);

    void onError(Throwable error);
}
