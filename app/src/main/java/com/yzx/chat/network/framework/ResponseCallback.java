package com.yzx.chat.network.framework;

import android.support.annotation.NonNull;



public interface ResponseCallback<T> {

    void onResponse(HttpRequest request,HttpResponse<T> response);

    void onError(@NonNull Throwable e);

    boolean isExecuteNextTask();

}
