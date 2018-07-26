package com.yzx.chat.network.framework;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;


/**
 * Created by YZX on 2018年05月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NetworkRunnable implements Runnable {

    private Call[] mCalls;
    private Handler mUIHandler;

    NetworkRunnable(Call[] call, Handler uiHandler) {
        mCalls = call;
        mUIHandler = uiHandler;
    }

    @Override
    public void run() {
        if (mCalls == null || mCalls.length == 0) {
            return;
        }
        for (Call call : mCalls) {
            if (call.isCancel()) {
                break;
            }
            if (NetworkExecutor.getNetworkConfigure().isEnableNetworkStateCheck && !NetworkUtil.isNetworkConnected(NetworkExecutor.sAppContext)) {
                callbackError(call.getResponseCallback(), new NetworkUnavailableException("Current network is unavailable."), call.isCallbackRunOnMainThread());
            } else {
                request(call);
            }
            ResponseCallback callback = call.getResponseCallback();
            if (call.isCancel() || (callback != null && !callback.isExecuteNextTask())) {
                break;
            }
        }
    }


    private void request(final Call call) {
        boolean isCallbackRunOnMainThread = call.isCallbackRunOnMainThread();
        boolean isDownload = call instanceof DownloadCall;
        ResponseCallback responseCallback = isDownload ? ((DownloadCall) call).getDownloadCallback() : call.getResponseCallback();
        HttpRequest request = call.request();
        HttpConverter converter = call.getHttpConverter();
        ResponseParams responseParams;
        if (isDownload) {
            DownloadCallback downloadCallback = (DownloadCallback) responseCallback;
            String savePath = ((DownloadCall) call).getSavePath();
            UniformVelocityInterpolator interpolator = null;
            if (downloadCallback != null) {
                interpolator = new UniformVelocityInterpolator(downloadCallback, mUIHandler);
            }
            responseParams = Http.callDownload(request, converter, savePath, interpolator, call);
        } else {
            responseParams = Http.call(request, converter);
        }

        if (responseCallback != null && !call.isCancel()) {
            final Throwable throwable = responseParams.throwable;
            if (throwable != null) {
                callbackError(responseCallback, throwable, isCallbackRunOnMainThread);
            } else {
                try {
                    HttpResponse response = responseParams.convert(converter, call.getGenericType());
                    callbackResponse(responseCallback, request, response, isCallbackRunOnMainThread);
                } catch (Exception e) {
                    callbackError(responseCallback, e, isCallbackRunOnMainThread);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void callbackResponse(final ResponseCallback responseCallback, final HttpRequest request, final HttpResponse response, boolean isRunOnMainThread) {
        if (isRunOnMainThread) {
            final CountDownLatch latch = new CountDownLatch(1);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    responseCallback.onResponse(request, response);
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            responseCallback.onResponse(request, response);
        }
    }

    private void callbackError(final ResponseCallback responseCallback, final Throwable throwable, boolean isRunOnMainThread) {
        if (isRunOnMainThread) {
            final CountDownLatch latch = new CountDownLatch(1);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    responseCallback.onError(throwable);
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            responseCallback.onError(throwable);
        }
    }

    private static class UniformVelocityInterpolator implements DownloadCallback {

        private static final int MIN_CALLBACK_INTERVAL_MS = 32;

        private DownloadCallback mDownloadCallback;
        private Handler mHandler;
        private long mLastCallbackProcessTimeMs;

        private UniformVelocityInterpolator(DownloadCallback downloadCallback, Handler handler) {
            mDownloadCallback = downloadCallback;
            mHandler = handler;
        }


        @Override
        public void onProcess(final int percent) {
            if (mDownloadCallback == null) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - mLastCallbackProcessTimeMs >= MIN_CALLBACK_INTERVAL_MS || percent == 100) {
                mLastCallbackProcessTimeMs = now;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadCallback.onProcess(percent);
                    }
                });
            }
        }

        @Override
        public void onResponse(HttpRequest request, HttpResponse<String> response) {

        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public boolean isExecuteNextTask() {
            return false;
        }
    }
}
