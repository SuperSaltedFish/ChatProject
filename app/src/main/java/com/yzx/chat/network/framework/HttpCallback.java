package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

public interface HttpCallback<T> {

    void onResponse(@Nullable T response);

    void onError(@Nullable String error);

    boolean isComplete();

}
