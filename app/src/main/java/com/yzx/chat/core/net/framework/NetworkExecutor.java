package com.yzx.chat.core.net.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NetworkExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static NetworkExecutor sNetworkExecutor;
    private static Handler sUIHandler;
    private static NetworkConfigure sNetworkConfigure;
    static Context sAppContext;

    public synchronized static void init(Context context) {
        sAppContext = context.getApplicationContext();
        if (sNetworkExecutor == null) {
            sNetworkExecutor = new NetworkExecutor();
            sUIHandler = new Handler(Looper.getMainLooper());
            sNetworkConfigure = new NetworkConfigure();
        }
    }

    public static NetworkExecutor getInstance() {
        if (sNetworkExecutor == null) {
            throw new RuntimeException("The NetworkExecutor is not initialized.");
        }
        return sNetworkExecutor;
    }

    public static NetworkConfigure getNetworkConfigure() {
        return sNetworkConfigure;
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
        mThreadPoolExecutor.execute(new NetworkRunnable(mCalls, sUIHandler));
    }

    public void cleanAllTask() {
        mThreadPoolExecutor.purge();
        mThreadPoolExecutor.getQueue().clear();
    }


}