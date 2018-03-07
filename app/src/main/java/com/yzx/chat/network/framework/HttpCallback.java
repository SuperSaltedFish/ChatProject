package com.yzx.chat.network.framework;

import android.support.annotation.NonNull;



public interface HttpCallback<T> {

    void onResponse(HttpResponse<T> response);

    void onError(@NonNull Throwable e);

    boolean isExecuteNextTask();

}
