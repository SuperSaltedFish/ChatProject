package com.yzx.chat.module.moments.view;


import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.MomentsAdapter;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * Created by YZX on 2017年08月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class MomentsFragment extends BaseFragment {

    public static final String TAG = MomentsFragment.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private MomentsAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.MomentsFragment_mRecyclerView);
        mSwipeRefreshLayout = parentView.findViewById(R.id.MomentsFragment_mSwipeRefreshLayout);
        mAdapter = new MomentsAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration((int) AndroidHelper.dip2px(12), SpacesItemDecoration.VERTICAL, true, true));
        // mRecyclerView.addOnScrollListener(new ImageAutoLoadScrollListener());
    }

    @Override
    protected void onFirstVisible() {

    }
}
