package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2018年01月28日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ImageSelectedAdapter extends BaseRecyclerViewAdapter<ImageSelectedAdapter.ImageSelectedHolder> {

    private List<String> mPicSelectedList;
    private int mCurrentSelectedPosition = -1;

    public ImageSelectedAdapter(List<String> picSelectedList) {
        mPicSelectedList = picSelectedList;
    }

    @Override
    public ImageSelectedHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ImageSelectedHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_selected, parent, false));
    }

    @Override
    public void bindDataToViewHolder(ImageSelectedHolder holder, int position) {
        holder.mSelectedImage.setSelected(position == mCurrentSelectedPosition);
        GlideUtil.loadFromUrl(mContext, holder.mSelectedImage, mPicSelectedList.get(position));
    }

    @Override
    public void onViewHolderRecycled(ImageSelectedHolder holder) {
        GlideUtil.clear(mContext,holder.mSelectedImage);
    }

    @Override
    public int getViewHolderCount() {
        return mPicSelectedList == null ? 0 : mPicSelectedList.size();
    }

    public void setSelected(int position) {
        if (mCurrentSelectedPosition != position) {
            notifyItemChangedEx(mCurrentSelectedPosition);
            if (position >= 0) {
                notifyItemChangedEx(position);
            }
            mCurrentSelectedPosition = position;
        }
    }

    final static class ImageSelectedHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        ImageView mSelectedImage;

        ImageSelectedHolder(View itemView) {
            super(itemView);
            mSelectedImage = (ImageView) itemView;
        }

    }
}
