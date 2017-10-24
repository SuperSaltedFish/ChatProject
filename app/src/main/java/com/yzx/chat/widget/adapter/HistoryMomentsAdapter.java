package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.widget.view.NineGridImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年08月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class HistoryMomentsAdapter extends BaseRecyclerViewAdapter<HistoryMomentsAdapter.ItemView> {


    public HistoryMomentsAdapter() {

    }


    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_history_moments, parent, false);
        return new ItemView(view);
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        List<String> s = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            s.add(new String("" + i));
        }
        if(s.size()==0){
            holder.mNineGridImageView.setVisibility(View.GONE);
        }else {
            holder.mNineGridImageView.setVisibility(View.VISIBLE);
        }
        holder.mNineGridImageView.setImageData(s);
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    final static class ItemView extends RecyclerView.ViewHolder {

        NineGridImageView mNineGridImageView;

        ItemView(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            mNineGridImageView = (NineGridImageView) itemView.findViewById(R.id.HistoryMomentsAdapter_mNineGridImageView);
        }

    }
}
