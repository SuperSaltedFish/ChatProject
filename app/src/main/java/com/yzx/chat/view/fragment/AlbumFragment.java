package com.yzx.chat.view.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.widget.adapter.AlbumAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.view.TimeLineItemDecoration;

/**
 * Created by YZX on 2017年09月02日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AlbumFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private AlbumAdapter mAlbumAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_album;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = (RecyclerView) parentView.findViewById(R.id.AlbumFragment_mRecyclerView);
        mAlbumAdapter = new AlbumAdapter();
    }

    @Override
    protected void setup() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAlbumAdapter);
        mRecyclerView.addItemDecoration(new TimeLineItemDecoration());
        mRecyclerView.setHasFixedSize(true);
    }

}
