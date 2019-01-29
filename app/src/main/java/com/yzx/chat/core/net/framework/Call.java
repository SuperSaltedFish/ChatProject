package com.yzx.chat.core.net.framework;


import android.support.annotation.Nullable;

import java.lang.reflect.Type;


public interface Call<T> extends Cancellable {

    boolean isCallbackRunOnMainThread();

    void setResponseCallback(@Nullable ResponseCallback<T> callback);

    void setResponseCallback(@Nullable ResponseCallback<T> callback, boolean runOnMainThread);

    ResponseCallback<T> getResponseCallback();

    HttpRequest request();

    void setHttpConverter(HttpConverter adapter);

    HttpConverter getHttpConverter();

    Type getGenericType();
}
