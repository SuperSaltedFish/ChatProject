package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.NineGridImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年08月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class MomentsAdapter extends BaseRecyclerViewAdapter<MomentsAdapter.ItemView> {

    public MomentsAdapter() {

    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_moments, parent, false);
        return new ItemView(view);
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        List<String> s = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            s.add("");
        }
        if(s.size()==0){
            holder.mNineGridImageView.setVisibility(View.GONE);
        }else {
            holder.mNineGridImageView.setVisibility(View.VISIBLE);
        }
        holder.mNineGridImageView.setImageData(s);
        GlideUtil.loadAvatarFromUrl(mContext,holder.mIvAvatar,R.drawable.temp_head_image);
    }

    @Override
    public void onViewHolderRecycled(ItemView holder) {
        GlideUtil.clear(mContext,holder.mIvAvatar);
    }

    @Override
    public int getViewHolderCount() {
        return 10;
    }


    final static class ItemView extends BaseRecyclerViewAdapter.BaseViewHolder {

        NineGridImageView mNineGridImageView;
        ImageView mIvAvatar;

        ItemView(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            mNineGridImageView = itemView.findViewById(R.id.MomentsAdapter_mNineGridImageContent);
            mIvAvatar = itemView.findViewById(R.id.MomentsAdapter_mIvAvatar);
        }

    }
}
