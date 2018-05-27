package com.yzx.chat.mvp.view.fragment;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.HistoryMomentsAdapter;
import com.yzx.chat.base.BaseFragment;


/**
 * Created by YZX on 2017年08月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class MomentsFragment extends BaseFragment {

    public static final String TAG = MomentsFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private HistoryMomentsAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.MomentsFragment_mRecyclerView);
        mAdapter = new HistoryMomentsAdapter();
    }

    @Override
    protected void setup() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onFirstVisible() {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.e("MomentsFragment:"+hidden);
        if(mParentView!=null){
//            mParentView.setFitsSystemWindows(!hidden);
//            mParentView.requestApplyInsets();
        }
        super.onHiddenChanged(hidden);
    }

}
