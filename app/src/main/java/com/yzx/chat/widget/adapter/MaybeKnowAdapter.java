package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;

/**
 * Created by YZX on 2017年11月21日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class MaybeKnowAdapter extends BaseRecyclerViewAdapter<MaybeKnowAdapter.MaybeKnowHolder> {

    @Override
    public MaybeKnowHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MaybeKnowHolder(LayoutInflater.from(mContext).inflate(R.layout.item_maybe_know, parent, false));
    }

    @Override
    public void bindDataToViewHolder(MaybeKnowHolder holder, int position) {
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, R.drawable.temp_head_image);
    }

    @Override
    public int getViewHolderCount() {
        return 12;
    }


    static class MaybeKnowHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        ImageView mIvAvatar;

        MaybeKnowHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.MaybeKnowAdapter_mIvAvatar);
        }
    }
}
