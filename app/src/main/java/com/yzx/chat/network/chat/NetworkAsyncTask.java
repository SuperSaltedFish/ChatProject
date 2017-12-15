package com.yzx.chat.network.chat;


import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yzx.chat.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by YZX on 2017年10月09日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class NetworkAsyncTask<LifeDependent, Param, Result> {

    protected void onPreExecute() {
    }

    protected abstract Result doInBackground(Param... params);

    protected void onPostExecute(Result result, LifeDependent lifeDependentObject) {
    }

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.min(2, CPU_COUNT);
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static final int MESSAGE_POST_RESULT = 0x1;

    private static final ThreadPoolExecutor mThreadPoolExecutor;

    static {
        mThreadPoolExecutor = new NetworkThreadPoll(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(64), new BackgroundThreadFactory());
        mThreadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    public static void cleanAllTask() {
        mThreadPoolExecutor.purge();
        mThreadPoolExecutor.getQueue().clear();
    }


    private WeakReference<LifeDependent> mLifeDependentReference;
    private InternalHandler mHandler;
    private Future<?> mFuture;
    private boolean isAlreadyExecute;
    private boolean isLifeCycleDependent;


    private AtomicBoolean isCancel;

    public NetworkAsyncTask(LifeDependent lifeCycleDependence) {
        if (lifeCycleDependence != null) {
            mLifeDependentReference = new WeakReference<>(lifeCycleDependence);
            isLifeCycleDependent = true;
        }
        mHandler = new InternalHandler(Looper.getMainLooper());
        isCancel = new AtomicBoolean(false);
    }

    @MainThread
    public synchronized void execute(Param... params) {
        if (isAlreadyExecute) {
            throw new RuntimeException("Cannot execute task: the task has already been executed");
        }
        if (isCancel()) {
            return;
        }
        onPreExecute();
        mFuture = mThreadPoolExecutor.submit(new WorkerRunnable(params));
        isAlreadyExecute = true;
    }

    public synchronized void cancel() {
        if (mFuture != null) {
            mFuture.cancel(true);
        }
        if (mLifeDependentReference != null) {
            mLifeDependentReference.clear();
        }
        isCancel.set(true);
    }

    public boolean isCancel() {
        if (isCancel.get()) {
            return true;
        } else if (mLifeDependentReference == null) {
            return false;
        } else if (mLifeDependentReference.get() == null) {
            return true;
        }
        return false;
    }

    public boolean isLifeCycleDependent() {
        return isLifeCycleDependent;
    }

    @Nullable
    private Object getLifeCycleObject() {
        if (mLifeDependentReference == null) {
            return null;
        } else {
            return mLifeDependentReference.get();
        }
    }

    public void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

    private synchronized void postResult(Result result) {
        Message message = mHandler.obtainMessage(MESSAGE_POST_RESULT, new TaskResult<>(NetworkAsyncTask.this, result));
        message.sendToTarget();
    }


    private class WorkerRunnable implements Runnable {

        private Param[] mParams;

        WorkerRunnable(Param[] params) {
            mParams = params;
        }

        @Override
        public void run() {
            if (isCancel()) {
                return;
            }
            Result result = doInBackground(mParams);
            Binder.flushPendingCommands();
            postResult(result);
        }
    }

    private static class InternalHandler extends Handler {

        InternalHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_POST_RESULT) {
                TaskResult<?> result = (TaskResult<?>) msg.obj;
                NetworkAsyncTask task = result.mTask;
                if (!task.isCancel() && (task.getLifeCycleObject() != null || task.isLifeCycleDependent())) {
                    task.onPostExecute(result.mData, task.getLifeCycleObject());
                }
            }
        }
    }

    private static class TaskResult<Result> {
        final NetworkAsyncTask mTask;
        final Result mData;

        TaskResult(NetworkAsyncTask task, Result result) {
            mTask = task;
            mData = result;
        }

    }

    private static class NetworkThreadPoll extends ThreadPoolExecutor {

        NetworkThreadPoll(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Future<?> f = (Future<?>) r;
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogUtil.e("InterruptedException" + e.toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
                LogUtil.e("ExecutionException:" + e.toString());
            }
        }

    }

    private static class BackgroundThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread();
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LogUtil.e(e.toString());
                    e.printStackTrace();
                }
            });
            return new Thread(r);
        }
    }

}
