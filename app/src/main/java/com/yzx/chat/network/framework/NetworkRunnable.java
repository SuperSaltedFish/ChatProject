package com.yzx.chat.network.framework;

import android.os.Handler;

import com.yzx.chat.util.LogUtil;


/**
 * Created by YZX on 2018年05月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NetworkRunnable implements Runnable {

    private Call<?>[] mCalls;
    private int mStartIndex;
    private int mLength;
    private NetworkExecutor mExecutor;
    private Handler mUIHandler;

    NetworkRunnable(Call<?>[] call, int startIndex, int length, NetworkExecutor executor, Handler uiHandler) {
        mCalls = call;
        mStartIndex = startIndex;
        mLength = length;
        mExecutor = executor;
        mUIHandler = uiHandler;
    }

    @Override
    public void run() {
        final Call call = mCalls[mStartIndex++];
        if (call.isCancel()) {
            return;
        }
        HttpDataFormatAdapter adapter = call.getHttpDataFormatAdapter();
        HttpRequest request = call.getHttpRequest();
        String strParams = adapter != null ? adapter.paramsToString(request) : null;
        String url = request.url();
        RequestType requestType = request.requestType();
        DownloadCallback downloadCallback = call.getDownloadCallback();
        DownloadProcessListenerImpl processListener = null;
        if (downloadCallback != null) {
            processListener = new DownloadProcessListenerImpl(downloadCallback, call.isDownloadCallbackRunOnMainThread() ? mUIHandler : null);
        }
        Http.Result result;
        switch (requestType) {
            case GET:
                result = Http.doGet(url, strParams);
                break;
            case POST:
                result = Http.doPost(url, strParams);
                break;
            case GET_DOWNLOAD:
                result = Http.doGetByDownload(url, strParams, request.savePath(), processListener);
                break;
            case POST_DOWNLOAD:
                result = Http.doPostByDownload(url, strParams, request.savePath(), processListener);
                break;
            case POST_MULTI_PARAMS:
                result = Http.doPostByMultiParams(url, adapter != null ? adapter.multiParamsFormat(request) : request.params());
                break;
            default:
                throw new RuntimeException("unknown request type:" + request.requestType());
        }
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            requestError(call, throwable);
        } else {
            HttpResponseImpl response = new HttpResponseImpl(call.getGenericType());
            int responseCode = result.getResponseCode();
            response.setResponseCode(responseCode);
            if (responseCode == 200) {
                if (requestType == RequestType.GET_DOWNLOAD || requestType == RequestType.POST_DOWNLOAD) {
                    response.setResponse(result.getDownloadPath());
                    requestSuccess(call, response);
                } else {
                    try {
                        Object toObject = adapter.responseToObject(url, result.getResponseContent(), call.getGenericType());
                        response.setResponse(toObject);
                        requestSuccess(call, response);
                    } catch (Exception e) {
                        requestError(call, e);
                    }
                }
            } else {
                requestSuccess(call, response);
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void requestSuccess(final Call call, final HttpResponse httpResponse) {
        switch (call.getHttpRequest().requestType()) {
            case GET:
            case POST:
            case POST_MULTI_PARAMS:
                if (call.isResponseCallbackRunOnMainThread()) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseSuccess(call, httpResponse);
                        }
                    });
                } else {
                    responseSuccess(call, httpResponse);
                }
                break;
            case GET_DOWNLOAD:
            case POST_DOWNLOAD:
                if (call.isDownloadCallbackRunOnMainThread()) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadSuccess(call, httpResponse);
                        }
                    });
                } else {
                    downloadSuccess(call, httpResponse);
                }
                break;
        }
    }

    private void requestError(final Call call, final Throwable e) {
        switch (call.getHttpRequest().requestType()) {
            case GET:
            case POST:
            case POST_MULTI_PARAMS:
                if (call.isResponseCallbackRunOnMainThread()) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseError(call, e);
                        }
                    });
                } else {
                    responseError(call, e);
                }
                break;
            case GET_DOWNLOAD:
            case POST_DOWNLOAD:
                if (call.isDownloadCallbackRunOnMainThread()) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadError(call, e);
                        }
                    });
                } else {
                    downloadError(call, e);
                }
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void downloadSuccess(final Call call, final HttpResponse httpResponse) {
        DownloadCallback downloadCallback = call.getDownloadCallback();
        if (!call.isCancel() && downloadCallback != null) {
            downloadCallback.onFinish(httpResponse);
            if (isExecuteNextTask(call)) {
                mExecutor.submit(mCalls[mStartIndex]);
            }
        }
    }

    private void downloadError(final Call call, final Throwable e) {
        DownloadCallback downloadCallback = call.getDownloadCallback();
        if (!call.isCancel() && downloadCallback != null) {
            downloadCallback.onDownloadError(e);
            if (isExecuteNextTask(call)) {
                mExecutor.submit(mCalls[mStartIndex]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void responseSuccess(final Call call, final HttpResponse httpResponse) {
        ResponseCallback responseCallback = call.getResponseCallback();
        if (!call.isCancel() && responseCallback != null) {
            responseCallback.onResponse(httpResponse);
            if (isExecuteNextTask(call)) {
                mExecutor.submit(mCalls[mStartIndex]);
            }
        }
    }

    private void responseError(final Call call, final Throwable e) {
        ResponseCallback responseCallback = call.getResponseCallback();
        if (!call.isCancel() && responseCallback != null) {
            responseCallback.onError(e);
            if (isExecuteNextTask(call)) {
                mExecutor.submit(mCalls[mStartIndex]);
            }
        }
    }

    private boolean isExecuteNextTask(final Call call) {
        ResponseCallback callback = call.getResponseCallback();
        return mStartIndex < mLength && !call.isCancel() && callback != null && callback.isExecuteNextTask();
    }

    private static class DownloadProcessListenerImpl implements Http.DownloadProcessListener {
        private static final int CALLBACK_FREQUENCY = 5000;
        private DownloadCallback mDownloadCallback;
        private Handler mHandler;
        private boolean isStarted;
        private long mLastCallbackTime;
        private long mAlreadyDownloadSize;
        private long mTotalSize;
        private int mCurrentPercent;

        private DownloadProcessListenerImpl(DownloadCallback downloadCallback, Handler handler) {
            mDownloadCallback = downloadCallback;
            mHandler = handler;
        }

        @Override
        public void onProcess(long alreadyDownloadSize, long totalSize) {
            if (totalSize == 0) {
                return;
            }
            mAlreadyDownloadSize = alreadyDownloadSize;
            mTotalSize = totalSize;
            if (!isStarted) {
                isStarted = true;
            }
            long nowTime = System.currentTimeMillis();
            int percent = (int) ((mAlreadyDownloadSize * 100.0 / mTotalSize));
            if (nowTime - mLastCallbackTime >= CALLBACK_FREQUENCY || percent == 100) {
                if (percent != mCurrentPercent) {
                    mCurrentPercent = percent;
                    mLastCallbackTime = nowTime;
                    if (mHandler != null) {
                        mHandler.post(mCallbackProcessRunnable);
                    } else {
                        mCallbackProcessRunnable.run();
                    }
                }
            }
        }

        private final Runnable mCallbackProcessRunnable = new Runnable() {
            @Override
            public void run() {
                mDownloadCallback.onProcess(mCurrentPercent);
            }
        };

    }
}
