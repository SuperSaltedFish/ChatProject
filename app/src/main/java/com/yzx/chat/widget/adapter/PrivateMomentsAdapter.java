package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.widget.view.NineGridImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年08月21日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class PrivateMomentsAdapter extends BaseRecyclerViewAdapter<PrivateMomentsAdapter.PrivateMomentsHolder> {

    public PrivateMomentsAdapter() {

    }

    @Override
    public PrivateMomentsHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_private_moments, parent, false);
        return new PrivateMomentsHolder(view);
    }

    @Override
    public void bindDataToViewHolder(PrivateMomentsHolder holder, int position) {
        List<String> s = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            s.add("");
        }
        if (s.size() == 0) {
            holder.mNineGridImageContent.setVisibility(View.GONE);
        } else {
            holder.mNineGridImageContent.setVisibility(View.VISIBLE);
        }
        holder.mNineGridImageContent.setImageData(s);

    }

    @Override
    public int getViewHolderCount() {
        return 10;
    }


    final static class PrivateMomentsHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        NineGridImageView mNineGridImageContent;

        PrivateMomentsHolder(View itemView) {
            super(itemView);
            mNineGridImageContent = itemView.findViewById(R.id.PrivateMomentsAdapter_mNineGridImageContent);
        }


    }
}
