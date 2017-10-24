package com.yzx.chat.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.widget.listener.onFragmentRequestListener;
import com.yzx.chat.network.framework.Call;


/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseFragment extends Fragment {

    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void init(View parentView);

    protected abstract void setView();


    public Context mContext;
    private View mParentView;
    private boolean isOnceVisible;

    private onFragmentRequestListener mOnFragmentRequestListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (mContext instanceof onFragmentRequestListener) {
            mOnFragmentRequestListener = (onFragmentRequestListener) mContext;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        if (mParentView == null) {
            mParentView = inflater.inflate(getLayoutID(), container, false);
            init(mParentView);
            setView();
        }
        return mParentView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
        mParentView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mOnFragmentRequestListener = null;
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

    public void requestActivity(int requestCode,Object arg){
        if(mOnFragmentRequestListener!=null){
            mOnFragmentRequestListener.onFragmentRequest(this,requestCode,arg);
        }
    }

    public void onFirstVisible() {}

    public View getParentView() {
        return mParentView;
    }
}
