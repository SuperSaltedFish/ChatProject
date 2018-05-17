package com.yzx.chat.network.framework;


import android.support.annotation.Nullable;

import java.lang.reflect.Type;


public interface Call<T> extends Cancellable {

    boolean isResponseCallbackRunOnMainThread();

    boolean isDownloadCallbackRunOnMainThread();

    void setResponseCallback(@Nullable ResponseCallback<T> callback);

    void setResponseCallback(@Nullable ResponseCallback<T> callback, boolean runOnMainThread);

    void setDownloadCallback(@Nullable DownloadCallback callback);

    void setDownloadCallback(@Nullable DownloadCallback callback, boolean runOnMainThread);

    ResponseCallback<T> getResponseCallback();

    DownloadCallback getDownloadCallback();

    HttpRequest getHttpRequest();

    void setHttpDataFormatAdapter(HttpDataFormatAdapter adapter);

    HttpDataFormatAdapter getHttpDataFormatAdapter();

    Type getGenericType();
}
