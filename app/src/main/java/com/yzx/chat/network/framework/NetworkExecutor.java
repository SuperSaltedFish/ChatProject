package com.yzx.chat.network.framework;

import android.os.Handler;
import android.os.Looper;

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
        mThreadPoolExecutor.execute(new NetworkRunnable(mCalls, 0, mCalls.length, this, sUIHandler));
    }

    public void submit(Call<?>[] mCalls, int startIndex, int length) {
        if (mCalls == null || length == 0 || startIndex == length) {
            return;
        }
        mThreadPoolExecutor.execute(new NetworkRunnable(mCalls, startIndex, length, this, sUIHandler));
    }

    public void cleanAllTask() {
        mThreadPoolExecutor.purge();
        mThreadPoolExecutor.getQueue().clear();
    }


}