package com.yzx.chat.core.net.framework;


public interface Call<T> {
    void enqueue(Callback<T> callback);

    void enqueue(Callback<T> callback, boolean callbackOnMainThread);

    void cancel();

    boolean isCancel();
}
