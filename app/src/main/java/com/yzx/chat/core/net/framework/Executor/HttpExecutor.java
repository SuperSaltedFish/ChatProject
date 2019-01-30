package com.yzx.chat.core.net.framework.Executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class HttpExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static HttpExecutor sHttpExecutor;

    public static synchronized HttpExecutor getInstance() {
        if (sHttpExecutor == null) {
            sHttpExecutor = new HttpExecutor();
        }
        return sHttpExecutor;
    }

    private ThreadPoolExecutor mThreadPoolExecutor;

    private HttpExecutor() {
        if (sHttpExecutor != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mThreadPoolExecutor = new ThreadPoolExecutor(
                0,
                MAXIMUM_POOL_SIZE,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
    }

    public void submit(final HttpRequest request, final ResponseCallback callback) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ResponseParams response = Http.call(request);
                if (callback != null) {
                    if (response.throwable == null) {
                        callback.onResponse(response.responseCode, response.body);
                    } else {
                        callback.onError(response.throwable);
                    }
                }
            }
        });
    }

    public void submit(final DownloadHttpRequest request, final DownloadCallback callback) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ResponseParams response = Http.callDownload(request, callback);
                if (callback != null) {
                    if (response.throwable == null) {
                        callback.onComplete(new String(response.body));
                    } else {
                        callback.onError(response.throwable);
                    }
                }
            }
        });
    }

    public void cleanAllTask() {
        mThreadPoolExecutor.purge();
        mThreadPoolExecutor.getQueue().clear();
    }


}