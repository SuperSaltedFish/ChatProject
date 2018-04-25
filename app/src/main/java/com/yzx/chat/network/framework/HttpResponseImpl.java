package com.yzx.chat.network.framework;


import android.support.annotation.Nullable;

import java.lang.reflect.Type;

/**
 * Created by YZX on 2017年10月14日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


class HttpResponseImpl implements HttpResponse {

    private int mResponseCode;
    private Object mResponse;
    private Type mGenericType;

    HttpResponseImpl(Type genericType) {
        mGenericType = genericType;
    }

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public void setResponse(Object response) {
        mResponse = response;
    }

    @Override
    public int getResponseCode() {
        return mResponseCode;
    }

    @Nullable
    @Override
    public Object getResponse() {
        return mResponse;
    }

    @Override
    public Type getGenericType() {
        return mGenericType;
    }
}
