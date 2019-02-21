package com.yzx.chat.module.me.view;

import android.os.Bundle;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.core.entity.TagEntity;
import com.yzx.chat.module.me.contract.MyTagListContract;
import com.yzx.chat.module.me.presenter.MyTagListPresenter;
import com.yzx.chat.widget.adapter.MyTagsListAdapter;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyTagListActivity extends BaseCompatActivity<MyTagListContract.Presenter> implements MyTagListContract.View {

    private RecyclerView mRvTags;
    private MyTagsListAdapter mAdapter;
    private List<TagEntity> mTagList;

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
        setTitle(R.string.MyTagListActivity_Title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvTags.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
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
    public void showAllTags(List<TagEntity> tagList) {
        mTagList.clear();
        if(tagList!=null&&tagList.size()>0){
            mTagList.addAll(tagList);
        }
        mAdapter.notifyDataSetChanged();
    }
}
