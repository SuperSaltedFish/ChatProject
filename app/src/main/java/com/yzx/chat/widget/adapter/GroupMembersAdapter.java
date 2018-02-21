package com.yzx.chat.widget.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;


/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class GroupMembersAdapter extends BaseRecyclerViewAdapter<GroupMembersAdapter.GroupMembersHolder> {

    private final int mSingleLineMaxCount;

    public GroupMembersAdapter(int singleLineMaxCount) {
        mSingleLineMaxCount = singleLineMaxCount;
    }

    @Override
    public GroupMembersHolder getViewHolder(ViewGroup parent, int viewType) {
//        int itemSize = parent.getWidth() / mSingleLineMaxCount;
//        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_member, parent, false);
//        view.setLayoutParams(new RecyclerView.LayoutParams(itemSize, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new GroupMembersHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group_member, parent, false));
    }

    @Override
    public void bindDataToViewHolder(GroupMembersHolder holder, int position) {

    }

    @Override
    public int getViewHolderCount() {
        return 8;
    }

    final static class GroupMembersHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        GroupMembersHolder(View itemView) {
            super(itemView);
        }


    }
}
