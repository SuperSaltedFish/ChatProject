package com.yzx.chat.mvp.view.fragment;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.MomentsAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.view.SpacesItemDecoration;


/**
 * Created by YZX on 2017年08月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class MomentsFragment extends BaseFragment {

    public static final String TAG = MomentsFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MomentsAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.MomentsFragment_mRecyclerView);
        mAdapter = new MomentsAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(12), SpacesItemDecoration.VERTICAL, true, true));
        mRecyclerView.addOnScrollListener(new ImageAutoLoadScrollListener());
    }

    @Override
    protected void onFirstVisible() {

    }


}
