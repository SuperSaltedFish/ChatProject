package com.yzx.chat.network.framework;


import android.support.annotation.Nullable;

import java.lang.reflect.Type;

public interface Call<T> {

    boolean complete(HttpResponse response);

    void cancel();

    boolean isCallbackRunOnMainThread();

    boolean isCancel();

    boolean isHasCallback();

    void setCallback(@Nullable HttpCallback<T> callback);

    void setCallback(@Nullable HttpCallback<T> callback, boolean runOnMainThread);

    HttpRequest getHttpRequest();

    void setHttpDataFormatAdapter(HttpDataFormatAdapter adapter);

    HttpDataFormatAdapter getHttpDataFormatAdapter();


    Type getGenericType();
}
