package com.yzx.chat.network.framework;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NetworkExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static volatile NetworkExecutor sNetworkExecutor;
    private static volatile Handler sUIHandler;

    public static NetworkExecutor getInstance() {
        if (sNetworkExecutor == null) {
            synchronized (NetworkExecutor.class) {
                if (sNetworkExecutor == null) {
                    sNetworkExecutor = new NetworkExecutor();
                    sUIHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sNetworkExecutor;
    }


    private ThreadPoolExecutor mThreadPoolExecutor;

    private NetworkExecutor() {
        if (sNetworkExecutor != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mThreadPoolExecutor = new ThreadPoolExecutor(
                0,
                MAXIMUM_POOL_SIZE,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
    }

    public void submit(Call<?>... mCalls) {
        if (mCalls == null || mCalls.length == 0) {
            return;
        }
        mThreadPoolExecutor.execute(new NetworkRunnable(mCalls, 0, mCalls.length, this));
    }

    public void submit(Call<?>[] mCalls, int startIndex, int length) {
        if (mCalls == null || length == 0 || startIndex == length) {
            return;
        }
        mThreadPoolExecutor.execute(new NetworkRunnable(mCalls, startIndex, length, this));
    }

    public void cleanAllTask() {
        mThreadPoolExecutor.purge();
        mThreadPoolExecutor.getQueue().clear();
    }


    private static class NetworkRunnable implements Runnable {
        private Call<?>[] mCalls;
        private int mStartIndex;
        private int mLength;
        private NetworkExecutor mExecutor;

        public NetworkRunnable(Call<?>[] call, int startIndex, int length, NetworkExecutor executor) {
            mCalls = call;
            mStartIndex = startIndex;
            mLength = length;
            mExecutor = executor;
        }

        @Override
        public void run() {
            final Call call = mCalls[mStartIndex++];
            if (call.isCancel()) {
                return;
            }
            HttpDataFormatAdapter adapter = call.getHttpDataFormatAdapter();
            HttpRequest request = call.getHttpRequest();
            Map<HttpParamsType, List<Pair<String, Object>>> params = request.params();
            Http.Result result;
            if (request.isMultiParams()) {
                params = adapter.multiParamsFormat(request.url(), params, request.requestMethod());
                result = Http.doPostByMultiParams(request.url(), params);
            } else {
                String strParams = adapter.paramsToString(request.url(), params.get(HttpParamsType.PARAMETER_HTTP), request.requestMethod());
                switch (request.requestMethod()) {
                    case "GET":
                        result = Http.doGet(request.url(), strParams);
                        break;
                    case "POST":
                        result = Http.doPost(request.url(), strParams);
                        break;
                    default:
                        throw new RuntimeException("unknown request method:" + request.requestMethod());
                }
            }
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                error(call, throwable);
            } else {
                HttpResponseImpl response = new HttpResponseImpl();
                int responseCode = result.getResponseCode();
                response.setResponseCode(responseCode);
                if (responseCode == 200) {
                    try {
                        Object toObject = adapter.responseToObject(request.url(), result.getResponseContent(), call.getGenericType());
                        response.setResponse(toObject);
                        success(call, response);
                    } catch (Exception e) {
                        error(call, e);
                    }
                } else {
                    success(call, response);
                }
            }
        }


        @SuppressWarnings("unchecked")
        private void success(final Call call, final HttpResponse httpResponse) {
            if (call.isCallbackRunOnMainThread()) {
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HttpCallback callback = call.getCallback();
                        if (!call.isCancel() && callback != null) {
                            callback.onResponse(httpResponse);
                            if (isExecuteNextTask(call)) {
                                mExecutor.submit(mCalls[mStartIndex]);
                            }
                        }
                    }
                });
            } else {
                HttpCallback callback = call.getCallback();
                if (!call.isCancel() && callback != null) {
                    callback.onResponse(httpResponse);
                    if (isExecuteNextTask(call)) {
                        mExecutor.submit(mCalls[mStartIndex]);
                    }
                }
            }
        }

        private void error(final Call call, final Throwable e) {
            if (call.isCallbackRunOnMainThread()) {
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HttpCallback callback = call.getCallback();
                        if (!call.isCancel() && callback != null) {
                            callback.onError(e);
                            if (isExecuteNextTask(call)) {
                                mExecutor.submit(mCalls[mStartIndex]);
                            }
                        }
                    }
                });
            } else {
                HttpCallback callback = call.getCallback();
                if (!call.isCancel() && callback != null) {
                    callback.onError(e);
                    if (isExecuteNextTask(call)) {
                        mExecutor.submit(mCalls[mStartIndex]);
                    }
                }
            }
        }

        private boolean isExecuteNextTask(final Call call) {
            HttpCallback callback = call.getCallback();
            return mStartIndex < mLength && !call.isCancel() && callback != null && callback.isExecuteNextTask();
        }
    }


}