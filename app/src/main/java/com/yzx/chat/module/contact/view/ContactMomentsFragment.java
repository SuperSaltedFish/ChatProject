package com.yzx.chat.module.contact.view;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.PrivateMomentsAdapter;
import com.yzx.chat.widget.view.SpacesItemDecoration;
import com.yzx.chat.widget.view.TimeLineItemDecoration;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactMomentsFragment extends BaseFragment {

    private static final String ARGUMENT_CONTENT_ID = "ContentID";

    public static ContactMomentsFragment newInstance(String contactID) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_CONTENT_ID, contactID);
        ContactMomentsFragment fragment = new ContactMomentsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private PrivateMomentsAdapter mAdapter;
    private View mFooterView;
    private TextView mTvLoadMoreHint;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_moments;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ContactMomentsFragment_mRecyclerView);
        mSwipeRefreshLayout = parentView.findViewById(R.id.ContactMomentsFragment_mSwipeRefreshLayout);
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mAdapter = new PrivateMomentsAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

        mTvLoadMoreHint.setTextColor(ContextCompat.getColor(mContext, R.color.textSecondaryColorBlackLight));

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //  mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration((int) AndroidHelper.dip2px(12)));
        mRecyclerView.addItemDecoration(
                new TimeLineItemDecoration()
                        .setTimeLineWidth((int) AndroidHelper.dip2px(56))
                        .setTimeLineColor(ContextCompat.getColor(mContext, R.color.dividerColorBlack))
                        .setLineWidth(1.2f)
                        .setTimePointDrawable(AndroidHelper.getDrawable(R.drawable.src_time_point))
                        .setTimePointOffsetY((int) AndroidHelper.dip2px(4))
                        .setHasFooterView(true)
                        .setTimePointSize((int) AndroidHelper.dip2px(28)));

        mAdapter.setFooterView(mFooterView);
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
