package com.yzx.chat.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;

import com.yzx.chat.network.chat.ResultCallback;

import java.lang.ref.WeakReference;

/**
 * Created by YZX on 2018年02月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public abstract class AsyncResult<R, T> implements ResultCallback<T> {

    @MainThread
    protected abstract void onSuccessResult(R dependent, T result);

    @MainThread
    protected abstract void onFailureResult(R dependent, String error);

    private WeakReference<R> mDependentWeakReference;

    public AsyncResult(R dependent) {
        if (dependent != null) {
            mDependentWeakReference = new WeakReference<>(dependent);
        }
    }

    public void cancel() {
        synchronized (this) {
            if (mDependentWeakReference != null) {
                mDependentWeakReference.clear();
            }
        }
    }

    @Override
    public void onSuccess(final T result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (mDependentWeakReference == null) {
                        onSuccessResult(null, result);
                    } else if (mDependentWeakReference.get() != null) {
                        onSuccessResult(mDependentWeakReference.get(), result);
                    }
                }
            }
        });
    }

    @Override
    public void onFailure(final String error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (mDependentWeakReference == null) {
                        onFailureResult(null, error);
                    } else if (mDependentWeakReference.get() != null) {
                        onFailureResult(mDependentWeakReference.get(), error);
                    }
                }
            }
        });
    }
}
