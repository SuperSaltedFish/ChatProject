package com.yzx.chat.core.net.framework;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface Callback<T> {
    void onResponse(T response);

    void onError(Throwable error);
}
