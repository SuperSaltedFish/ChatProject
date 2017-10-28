package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

public interface HttpResponse<T> {

    int getResponseCode();

    @Nullable
    T getResponse();

}
