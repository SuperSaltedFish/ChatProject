package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.TagBean;
import com.yzx.chat.mvp.contract.MyTagListContract;
import com.yzx.chat.mvp.presenter.MyTagListPresenter;
import com.yzx.chat.widget.adapter.MyTagsListAdapter;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MyTagListActivity extends BaseCompatActivity<MyTagListContract.Presenter> implements MyTagListContract.View {

    private RecyclerView mRvTags;
    private MyTagsListAdapter mAdapter;
    private List<TagBean> mTagList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_my_tag_list;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvTags = findViewById(R.id.MyTagListActivity_mRvTags);
        mTagList = new ArrayList<>(16);
        mAdapter = new MyTagsListAdapter(mTagList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvTags.setAdapter(mAdapter);
        mRvTags.setHasFixedSize(true);
        mRvTags.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack), DividerItemDecoration.HORIZONTAL));
        mRvTags.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        mRvTags.addOnScrollListener(new ImageAutoLoadScrollListener());

        mPresenter.loadAllTagList();
    }

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {

        }
    };

    @Override
    public MyTagListContract.Presenter getPresenter() {
        return new MyTagListPresenter();
    }

    @Override
    public void showAllTags(List<TagBean> tagList) {
        mTagList.clear();
        if(tagList!=null&&tagList.size()>0){
            mTagList.addAll(tagList);
        }
        mAdapter.notifyDataSetChanged();
    }
}
