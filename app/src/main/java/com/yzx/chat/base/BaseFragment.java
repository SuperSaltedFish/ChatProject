package com.yzx.chat.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class BaseFragment<P extends BasePresenter> extends Fragment {

    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void init(View parentView);

    protected abstract void setup();

    protected P mPresenter;

    public Context mContext;
    private View mParentView;
    private boolean isOnceVisible;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        if (mParentView == null) {
            mParentView = inflater.inflate(getLayoutID(), container, false);
            initPresenter();
            init(mParentView);
            setup();
        }
        return mParentView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getUserVisibleHint() && !isOnceVisible) {
            isOnceVisible = true;
            onFirstVisible();
        }
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        mParentView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mParentView == null) {
            return;
        }
        if (!isOnceVisible && isVisibleToUser) {
            isOnceVisible = true;
            onFirstVisible();
        }
    }


    protected void onFirstVisible() {
    }

    public View getParentView() {
        return mParentView;
    }

    @SuppressWarnings("unchecked")
    private void initPresenter() {
        Type type = this.getClass().getGenericSuperclass();
        if (this instanceof BaseView) {
            BaseView view = (BaseView) this;
            mPresenter = (P) view.getPresenter();
            if (mPresenter != null && type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type genericType = parameterizedType.getActualTypeArguments()[0];
                Class<?>[] interfaces = mPresenter.getClass().getInterfaces();
                for (Class c : interfaces) {
                    if (c == genericType) {
                        mPresenter.attachView(view);
                        return;
                    }
                }
                mPresenter = null;
            }
        }
    }
}
