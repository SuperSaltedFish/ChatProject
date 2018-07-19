package com.yzx.chat.network.framework;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Type;

/**
 * Created by YZX on 2018年07月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class DownloadCallImpl implements DownloadCall {

    private DownloadCallback mDownloadCallback;
    private HttpRequestImpl mRequestParams;
    private String mSavePath;
    private HttpConverter mHttpConverter;
    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;

    DownloadCallImpl(HttpRequestImpl requestParams, HttpConverter httpConverter) {
        mRequestParams = requestParams;
        mHttpConverter = httpConverter;
    }

    @Override
    public void setSavePath(@NonNull String savePath) {
        if (TextUtils.isEmpty(savePath)) {
            throw new RuntimeException("Save path can not be empty");
        }
        mSavePath = savePath;
    }

    @Override
    public void setDownloadCallback(@Nullable DownloadCallback callback) {
        setDownloadCallback(callback, true);
    }

    @Override
    public void setDownloadCallback(@Nullable DownloadCallback callback, boolean runOnMainThread) {
        mDownloadCallback = callback;
        isCallbackRunOnMainThread = runOnMainThread;
    }

    @Override
    public String getSavePath() {
        return mSavePath;
    }

    @Override
    public boolean isCallbackRunOnMainThread() {
        return isCallbackRunOnMainThread;
    }

    @Override
    public DownloadCallback getDownloadCallback() {
        return mDownloadCallback;
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
        mHttpConverter= adapter;
    }

    @Override
    public HttpConverter getHttpConverter() {
        return mHttpConverter;
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

    @Override
    public void setResponseCallback(@Nullable ResponseCallback callback) {

    }

    @Override
    public void setResponseCallback(@Nullable ResponseCallback callback, boolean runOnMainThread) {

    }

    @Override
    public ResponseCallback getResponseCallback() {
        return null;
    }

    @Override
    public Type getGenericType() {
        return null;
    }

    private void destroy() {
        mRequestParams=null;
        mDownloadCallback = null;
        mHttpConverter = null;
    }
}
