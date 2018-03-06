package com.yzx.chat.network.chat;

/**
 * Created by YZX on 2018年02月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public interface ResultCallback<T> {
    void onSuccess(T result);

    void onFailure(String error);
}
