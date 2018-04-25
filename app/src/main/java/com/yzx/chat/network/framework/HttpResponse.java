package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;


public interface HttpResponse<T> {

    int getResponseCode();

    @Nullable
    T getResponse();

    Type getGenericType();

}
