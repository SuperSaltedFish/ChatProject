package com.yzx.chat.widget.listener;

import android.support.v7.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * Created by YZX on 2017年09月10日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class AutoEnableOverScrollListener extends RecyclerView.OnScrollListener {

    private SmartRefreshLayout mSmartRefreshLayout;

    public AutoEnableOverScrollListener(SmartRefreshLayout smartRefreshLayout) {
        mSmartRefreshLayout = smartRefreshLayout;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        setEnableOverScroll(RecyclerView.SCROLL_STATE_SETTLING != newState);
    }

    public void setEnableOverScroll(boolean isEnable) {
        mSmartRefreshLayout.setEnableRefresh(isEnable);
        mSmartRefreshLayout.setEnableLoadmore(isEnable);
    }
}
