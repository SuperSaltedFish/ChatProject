package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CallImpl<T> implements Call<T> {

    private WeakReference<HttpCallback<T>> mCallbackWeakReference;
    private HttpRequest mHttpRequest;
    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;
    private HttpDataFormatAdapter mAdapter;
    private Type mGenericType;

    public CallImpl(HttpRequest httpRequest, HttpDataFormatAdapter adapter) {
        mHttpRequest = httpRequest;
        mAdapter = adapter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean complete(HttpResponse response) {
        boolean isComplete = false;
        if (isCancel || mCallbackWeakReference == null) {
            isComplete = false;
        } else {
            HttpCallback<T> callback = mCallbackWeakReference.get();
            if (callback != null) {
                if (response.isSuccess()) {
                    callback.onResponse((T) response.getResponse());
                } else {
                    callback.onError(response.getError());
                }
                isComplete = callback.isComplete();
            }
        }
        destroy();
        return isComplete;
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
    public boolean isHasCallback() {
        return mCallbackWeakReference != null && mCallbackWeakReference.get() != null;
    }

    @Override
    public void setCallback(@Nullable HttpCallback<T> callback) {
        setCallback(callback, true);
    }

    @Override
    public void setCallback(@Nullable HttpCallback<T> callback, boolean runOnMainThread) {
        if (mCallbackWeakReference != null) {
            mCallbackWeakReference.clear();
            mCallbackWeakReference = null;
        }
        if (callback != null) {
            mCallbackWeakReference = new WeakReference<>(callback);
        }
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
        if (mCallbackWeakReference != null) {
            mCallbackWeakReference.clear();
            mCallbackWeakReference = null;
        }
        mAdapter = null;
    }

}
