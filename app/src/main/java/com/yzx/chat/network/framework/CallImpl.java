package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;


class CallImpl<T> implements Call<T> {

    private ResponseCallback<T> mResponseCallback;
    private DownloadCallback mDownloadCallback;
    private HttpRequest mHttpRequest;
    private HttpDataFormatAdapter mAdapter;
    private Type mGenericType;
    private boolean isCancel;
    private boolean isResponseCallbackRunOnMainThread;
    private boolean isDownloadCallbackRunOnMainThread;

    CallImpl(HttpRequest httpRequest, HttpDataFormatAdapter adapter, Type genericType) {
        mHttpRequest = httpRequest;
        mAdapter = adapter;
        mGenericType = genericType;
    }


    @Override
    public boolean isResponseCallbackRunOnMainThread() {
        return isResponseCallbackRunOnMainThread;
    }

    @Override
    public boolean isDownloadCallbackRunOnMainThread() {
        return isDownloadCallbackRunOnMainThread;
    }

    @Override
    public void setResponseCallback(@Nullable ResponseCallback<T> callback) {
        setResponseCallback(callback, true);
    }

    @Override
    public void setResponseCallback(@Nullable ResponseCallback<T> callback, boolean runOnMainThread) {
        mResponseCallback = callback;
        isResponseCallbackRunOnMainThread = runOnMainThread;
    }

    @Override
    public void setDownloadCallback(@Nullable DownloadCallback callback) {
        setDownloadCallback(callback, true);
    }

    @Override
    public void setDownloadCallback(@Nullable DownloadCallback callback, boolean runOnMainThread) {
        mDownloadCallback = callback;
        isDownloadCallbackRunOnMainThread = runOnMainThread;
    }

    @Override
    public ResponseCallback<T> getResponseCallback() {
        return mResponseCallback;
    }

    @Override
    public DownloadCallback getDownloadCallback() {
        return mDownloadCallback;
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
        mResponseCallback = null;
        mAdapter = null;
        mHttpRequest = null;
    }

}
