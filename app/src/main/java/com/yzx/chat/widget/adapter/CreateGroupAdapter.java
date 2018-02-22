package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

/**
 * Created by YZX on 2018年02月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CreateGroupAdapter extends BaseRecyclerViewAdapter<CreateGroupAdapter.CreateGroupHolder> {


    @Override
    public CreateGroupHolder getViewHolder(ViewGroup parent, int viewType) {
        return new CreateGroupHolder(LayoutInflater.from(mContext).inflate(R.layout.item_create_group, parent, false));
    }

    @Override
    public void bindDataToViewHolder(CreateGroupHolder holder, int position) {

    }

    @Override
    public int getViewHolderCount() {
        return 0;
    }

    final static class CreateGroupHolder extends BaseRecyclerViewAdapter.BaseViewHolder {


        CreateGroupHolder(View itemView) {
            super(itemView);


        }

    }
}
