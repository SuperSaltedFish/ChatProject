package com.yzx.chat.core.net.framework;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.yzx.chat.core.net.framework.Executor.DownloadHttpRequest;
import com.yzx.chat.core.net.framework.Executor.HttpExecutor;
import com.yzx.chat.core.net.framework.Executor.HttpRequest;
import com.yzx.chat.core.net.framework.Executor.ResponseCallback;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;


class CallImpl<T> implements Call<T>, ResponseCallback {

    private static final HttpExecutor NETWORK_EXECUTOR = HttpExecutor.getInstance();

    private HttpRequest mHttpRequest;
    private Type mGenericType;
    private HttpConverter mHttpConverter;
    private Callback<T> mCallback;

    private Handler mUIHandler;

    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;

    CallImpl(HttpRequest httpRequest, Type genericType, HttpConverter httpConverter) {
        mHttpRequest = httpRequest;
        mGenericType = genericType;
        mHttpConverter = httpConverter;
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void enqueue(Callback<T> callback) {
        enqueue(callback, true);
    }

    @Override
    public void enqueue(Callback<T> callback, boolean callbackOnMainThread) {
        mCallback = callback;
        isCallbackRunOnMainThread = callbackOnMainThread;
        NETWORK_EXECUTOR.submit(mHttpRequest, this);
    }

    @Override
    public void cancel() {
        isCancel = true;
        if (mHttpRequest instanceof DownloadHttpRequest) {
            ((DownloadHttpRequest) mHttpRequest).cancel();
        }
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResponse(int responseCode, @Nullable final byte[] body) {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            onError(new ResponseException("Http Response " + responseCode));
        } else {
            if (mCallback != null) {
                if (isCallbackRunOnMainThread) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onResponse((T) mHttpConverter.convertResponseBody(body, mGenericType));
                        }
                    });
                } else {
                    mCallback.onResponse((T) mHttpConverter.convertResponseBody(body, mGenericType));
                }
            }
        }
    }

    @Override
    public void onError(final Throwable error) {
        if (mCallback != null) {
            if (isCallbackRunOnMainThread) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onError(error);
                    }
                });
            } else {
                mCallback.onError(error);
            }
        }
    }
}
