package com.yzx.chat.widget.listener;

import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.listener.ResultCallback;

import java.lang.ref.WeakReference;


/**
 * Created by YZX on 2018年12月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class LifecycleMVPResultCallback<T> implements ResultCallback<T> {

    private WeakReference<BaseView> mLifecycleView;
    private boolean isEnableLoading;//自动启动和关闭加载对话框
    private boolean isAutoDismissLoadingInSuccessful;//是否再返回成功的时候关闭加载对话框，用于一些多个接口连续掉用而只启动一个加载对话框的场景

    public LifecycleMVPResultCallback(BaseView lifecycleView) {
        this(lifecycleView, true);
    }

    public LifecycleMVPResultCallback(BaseView lifecycleView, boolean isEnableLoading) {
        this(lifecycleView, isEnableLoading, true);
    }

    public LifecycleMVPResultCallback(BaseView lifecycleView, boolean isEnableLoading, boolean isAutoDismissLoadingInSuccessful) {
        mLifecycleView = new WeakReference<>(lifecycleView);
        this.isEnableLoading = isEnableLoading;
        this.isAutoDismissLoadingInSuccessful = isAutoDismissLoadingInSuccessful;
        if (isEnableLoading && lifecycleView != null) {
            lifecycleView.setEnableLoading(true);
        }
    }


    public final void onResult(T result) {
        BaseView view = mLifecycleView.get();
        if (view != null && view.isAttachedToPresenter()) {
            if (isEnableLoading && isAutoDismissLoadingInSuccessful) {
                view.setEnableLoading(false);
            }
            onSuccess(result);
            mLifecycleView.clear();
        }
    }

    @Override
    public final void onFailure(int code, String error) {
        BaseView view = mLifecycleView.get();
        if (view != null && view.isAttachedToPresenter()) {
            if (isEnableLoading) {
                view.setEnableLoading(false);
            }
            if (!onError(code, error)) {
                view.showErrorDialog(error);
            }
            mLifecycleView.clear();
        }
    }

    protected abstract void onSuccess(T result);

    protected boolean onError(int code, String error) {
        return false;
    }


}
