package com.yzx.chat.network.framework;

import android.os.Handler;
import android.os.Looper;

import com.yzx.chat.util.LogUtil;

import java.lang.reflect.Type;
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

    public void cleanAllTask(){
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
            final HttpRequest request = call.getHttpRequest();
            String resultParams = adapter.requestToString(request.url(), request.getParams(), request.requestMethod());
            Http.Result result;
            if ("POST".equals(request.requestMethod())) {
                result = Http.doPost(request.url(), resultParams);
            } else {
                result = Http.doGet(request.url(), resultParams);
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
                }else {
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
                            if(isExecuteNextTask(call)){
                                mExecutor.submit(mCalls[mStartIndex]);
                            }
                        }
                    }
                });
            } else {
                HttpCallback callback = call.getCallback();
                if (!call.isCancel() && callback != null) {
                    callback.onResponse(httpResponse);
                    if(isExecuteNextTask(call)){
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
                            if(isExecuteNextTask(call)){
                                mExecutor.submit(mCalls[mStartIndex]);
                            }
                        }
                    }
                });
            } else {
                HttpCallback callback = call.getCallback();
                if (!call.isCancel() && callback != null) {
                    callback.onError(e);
                    if(isExecuteNextTask(call)){
                        mExecutor.submit(mCalls[mStartIndex]);
                    }
                }
            }
        }

        private boolean isExecuteNextTask(final Call call) {
            HttpCallback callback = call.getCallback();
            return mStartIndex<mLength&&!call.isCancel() && callback != null && callback.isExecuteNextTask();
        }
    }


}


//package com.yzx.chat.network.framework;
//
//        import android.os.Handler;
//        import android.os.Looper;
//        import java.util.concurrent.ArrayBlockingQueue;
//        import java.util.concurrent.ThreadPoolExecutor;
//        import java.util.concurrent.TimeUnit;
//
//public class NetworkExecutor {
//
//    private static volatile NetworkExecutor sNetworkExecutor;
//    private static volatile Handler sUIHandler;
//
//    public static NetworkExecutor getInstance() {
//        if (sNetworkExecutor == null) {
//            synchronized (NetworkExecutor.class) {
//                if (sNetworkExecutor == null) {
//                    sNetworkExecutor = new NetworkExecutor();
//                    sUIHandler = new Handler(Looper.getMainLooper());
//                }
//            }
//        }
//        return sNetworkExecutor;
//    }
//
//
//    private ThreadPoolExecutor mThreadPoolExecutor;
//
//    private NetworkExecutor() {
//        if (sNetworkExecutor != null) {
//            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
//        }
//        mThreadPoolExecutor = new ThreadPoolExecutor(
//                0,
//                4,
//                30, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<Runnable>(32));
//    }
//
//    public void submit(Call<?> mCall) {
//        mThreadPoolExecutor.execute(new NetworkRunnable(mCall));
//    }
//
//    private static class NetworkRunnable implements Runnable {
//        private Call<?> mCall;
//
//        NetworkRunnable(Call<?> call) {
//            mCall = call;
//        }
//
//        @Override
//        public void run() {
//            if (mCall.isCancel()) {
//                return;
//            }
//            HttpDataFormatAdapter adapter = mCall.getHttpDataFormatAdapter();
//            final HttpRequest request = mCall.getHttpRequest();
//            String resultParams = adapter.requestToString(request.url(), request.getParams(), request.requestMethod());
//            String result;
//            if ("POST".equals(request.requestMethod())) {
//                result = Http.doPost(request.url(), resultParams);
//            } else {
//                result = Http.doGet(request.url(), resultParams);
//            }
//
//            if (!mCall.isHasCallback()) {
//                return;
//            }
//            final HttpResponseImpl response = new HttpResponseImpl();
//            if (result != null) {
//                Object toObject = adapter.responseToObject(request.url(), result, mCall.getGenericType());
//                response.setSuccess(true);
//                response.setResponse(toObject);
//            }
//            if (mCall.isCallbackRunOnMainThread()) {
//                sUIHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!mCall.isCancel()) {
//                            mCall.complete(response);
//                        }
//                    }
//                });
//            } else {
//                if (!mCall.isCancel()) {
//                    mCall.complete(response);
//                }
//            }
//
//
//        }
//
//
//    }
//
//
//}
