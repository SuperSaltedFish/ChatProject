package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LocalSingleImageAdapter extends BaseRecyclerViewAdapter<LocalSingleImageAdapter.ItemView> {

    private List<String> mImagePathList;
    private int mHorizontalItemCount;

    public LocalSingleImageAdapter(List<String> imagePathList, int horizontalItemCount) {
        mImagePathList = imagePathList;
        mHorizontalItemCount = horizontalItemCount;
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        int itemSize = parent.getWidth() / mHorizontalItemCount;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_local_image, parent, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(itemSize, itemSize));
        return new ItemView(view);
    }

    @Override
    public void bindDataToViewHolder(LocalSingleImageAdapter.ItemView holder, int position) {
        GlideUtil.loadFromUrl(mContext, holder.mIvImage, String.format("file://%s", mImagePathList.get(position)));
    }

    @Override
    public int getViewHolderCount() {
        return mImagePathList == null ? 0 : mImagePathList.size();
    }


    final static class ItemView extends BaseRecyclerViewAdapter.BaseViewHolder {

        ImageView mIvImage;

        ItemView(View itemView) {
            super(itemView);
            mIvImage = itemView.findViewById(R.id.ImageSelectorActivity_mIvImage);

        }
    }

}
