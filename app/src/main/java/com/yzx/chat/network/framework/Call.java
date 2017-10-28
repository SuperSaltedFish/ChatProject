package com.yzx.chat.network.framework;


import android.support.annotation.Nullable;

import java.lang.reflect.Type;

public interface Call<T> {

    void cancel();

    boolean isCallbackRunOnMainThread();

    boolean isCancel();

    void setCallback(@Nullable HttpCallback<T> callback);

    void setCallback(@Nullable HttpCallback<T> callback, boolean runOnMainThread);

    HttpCallback<T> getCallback();

    HttpRequest getHttpRequest();

    void setHttpDataFormatAdapter(HttpDataFormatAdapter adapter);

    HttpDataFormatAdapter getHttpDataFormatAdapter();

    Type getGenericType();
}
