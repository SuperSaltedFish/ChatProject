package com.yzx.chat.view.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.widget.adapter.HistoryMomentsAdapter;
import com.yzx.chat.base.BaseFragment;

/**
 * Created by YZX on 2017年09月24日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class FriendMomentsFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private HistoryMomentsAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_friend_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = (RecyclerView) parentView.findViewById(R.id.HistoryMomentsFragment_mRecyclerView);
        mAdapter = new HistoryMomentsAdapter();
    }

    @Override
    protected void setView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onFirstVisible() {

    }

}
