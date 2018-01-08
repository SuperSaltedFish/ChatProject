package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

/**
 * Created by YZX on 2017年11月21日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class NewContactAdapter extends BaseRecyclerViewAdapter<NewContactAdapter.NewContactHolder> {

    @Override
    public NewContactHolder getViewHolder(ViewGroup parent, int viewType) {
        return new NewContactHolder(LayoutInflater.from(mContext).inflate(R.layout.item_new_contact, parent, false));
    }

    @Override
    public void bindDataToViewHolder(NewContactHolder holder, int position) {

    }

    @Override
    public int getViewHolderCount() {
        return 12;
    }


    static class NewContactHolder extends BaseRecyclerViewAdapter.BaseViewHolder {


        NewContactHolder(View itemView) {
            super(itemView);
        }
    }
}
