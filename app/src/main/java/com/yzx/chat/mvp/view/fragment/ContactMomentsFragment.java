package com.yzx.chat.mvp.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.MomentsAdapter;
import com.yzx.chat.widget.view.TimeLineItemDecoration;

/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactMomentsFragment extends BaseFragment {

    private static final String ARGUMENT_CONTENT_ID = "ContentID";

    public static ContactMomentsFragment newInstance(String contactID) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_CONTENT_ID,contactID);
        ContactMomentsFragment fragment = new ContactMomentsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private MomentsAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ContactMomentsFragment_mRecyclerView);
        mSwipeRefreshLayout = parentView.findViewById(R.id.ContactMomentsFragment_mSwipeRefreshLayout);
        mAdapter = new MomentsAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {


        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //  mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(
                new TimeLineItemDecoration()
                        .setTimeLineWidth((int) AndroidUtil.dip2px(32))
                        .setTimeLineColor(ContextCompat.getColor(mContext, R.color.dividerColorBlack))
                        .setLineWidth(1)
                    //    .setTimePointDrawable(AndroidUtil.getDrawable(R.drawable.temp3))
                        .setTimePointOffsetY((int) AndroidUtil.dip2px(16))
                        .setTimePointSize((int) AndroidUtil.dip2px(24)));
    }

    @Override
    protected void onFirstVisible() {
        mSwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnRefreshListener.onRefresh();
            }
        }, 1000);
    }

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mRecyclerView.getAdapter() == null) {
                mRecyclerView.setAdapter(mAdapter);
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };
}
