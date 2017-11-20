package com.yzx.chat.network.chat;


import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by YZX on 2017年10月09日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class NetworkAsyncTask<Params, Result> {

    protected void onPreExecute() {
    }

    protected abstract Result doInBackground(Params... params);

    protected void onPostExecute(Result result, Object lifeCycleObject) {
    }

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.min(1, CPU_COUNT);
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static final int MESSAGE_POST_RESULT = 0x1;

    private static final ThreadPoolExecutor mThreadPoolExecutor;

    static {
        mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(64), new BackgroundThreadFactory());
        mThreadPoolExecutor.allowCoreThreadTimeOut(true);
    }


    private WeakReference<Object> mLifeCycleReference;
    private InternalHandler mHandler;
    private Future<?> mFuture;
    private boolean isAlreadyExecute;


    private AtomicBoolean isCancel;

    public NetworkAsyncTask(Object lifeCycleDependence) {
        if (lifeCycleDependence != null) {
            mLifeCycleReference = new WeakReference<>(lifeCycleDependence);
        }
        mHandler = new InternalHandler(Looper.getMainLooper());
        isCancel = new AtomicBoolean(false);
    }

    @MainThread
    public synchronized void execute(Params... params) {
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
        if (mLifeCycleReference != null) {
            mLifeCycleReference.clear();
        }
        isCancel.set(true);
    }

    public boolean isCancel() {
        if (isCancel.get()) {
            return true;
        } else if (mLifeCycleReference == null) {
            return false;
        } else if (mLifeCycleReference.get() == null) {
            return true;
        }
        return false;
    }

    @Nullable
    private Object getLifeCycleObject() {
        if (mLifeCycleReference == null) {
            return null;
        } else {
            return mLifeCycleReference.get();
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

        private Params[] mParams;

        WorkerRunnable(Params[] params) {
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
                if (!task.isCancel() && task.getLifeCycleObject() != null) {
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

    private static class BackgroundThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread();
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return new Thread(r);
        }
    }

}
