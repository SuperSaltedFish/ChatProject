package com.yzx.chat.core.net.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;


class CallImpl<T> implements Call<T> {

    private ResponseCallback<T> mResponseCallback;
    private HttpRequest mRequestParams;
    private Type mGenericType;
    private HttpConverter mHttpConverter;
    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;

    CallImpl(HttpRequest httpRequest, Type genericType, HttpConverter httpConverter) {
        mRequestParams = httpRequest;
        mGenericType = genericType;
        mHttpConverter = httpConverter;
    }


    @Override
    public boolean isCallbackRunOnMainThread() {
        return isCallbackRunOnMainThread;
    }

    @Override
    public void setResponseCallback(@Nullable ResponseCallback<T> callback) {
        setResponseCallback(callback, true);
    }

    @Override
    public void setResponseCallback(@Nullable ResponseCallback<T> callback, boolean runOnMainThread) {
        mResponseCallback = callback;
        isCallbackRunOnMainThread = runOnMainThread;
    }

    @Override
    public ResponseCallback<T> getResponseCallback() {
        return mResponseCallback;
    }

    @Override
    public HttpRequest request() {
        return mRequestParams;
    }

    @Override
    public void setHttpConverter(HttpConverter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("HttpConverter == null");
        }
        mHttpConverter = adapter;
    }

    @Override
    public HttpConverter getHttpConverter() {
        return mHttpConverter;
    }

    @Override
    public Type getGenericType() {
        return mGenericType;
    }

    @Override
    public void cancel() {
        isCancel = true;
        destroy();
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    private void destroy() {
        mRequestParams=null;
        mResponseCallback = null;
        mHttpConverter = null;
    }

}
