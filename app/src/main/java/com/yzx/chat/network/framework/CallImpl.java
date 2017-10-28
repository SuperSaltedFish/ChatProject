package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;

public class CallImpl<T> implements Call<T> {

    private HttpCallback<T> mCallback;
    private HttpRequest mHttpRequest;
    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;
    private HttpDataFormatAdapter mAdapter;
    private Type mGenericType;

    public CallImpl(HttpRequest httpRequest, HttpDataFormatAdapter adapter,Type genericType) {
        mHttpRequest = httpRequest;
        mAdapter = adapter;
        mGenericType = genericType;
    }

    @Override
    public void cancel() {
        isCancel = true;
        destroy();
    }

    @Override
    public boolean isCallbackRunOnMainThread() {
        return isCallbackRunOnMainThread;
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    @Override
    public void setCallback(@Nullable HttpCallback<T> callback) {
        setCallback(callback, true);
    }

    @Override
    public void setCallback(@Nullable HttpCallback<T> callback, boolean runOnMainThread) {
        mCallback = callback;
        isCallbackRunOnMainThread = runOnMainThread;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return mHttpRequest;
    }

    @Override
    public void setHttpDataFormatAdapter(HttpDataFormatAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public HttpDataFormatAdapter getHttpDataFormatAdapter() {
        return mAdapter;
    }

    @Override
    public void setGenericType(Type type) {
        mGenericType = type;
    }

    @Override
    public Type getGenericType() {
        return mGenericType;
    }

    private void destroy() {
        mCallback =null;
        mAdapter = null;
        mHttpRequest =null;
    }

}
