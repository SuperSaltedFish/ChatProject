package com.yzx.chat.widget.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2017年08月14日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class LocalMultiImageAdapter extends BaseRecyclerViewAdapter<LocalMultiImageAdapter.ItemView> {

    private List<String> mImagePathList;
    private List<String> mImageSelectedList;
    private final int mHorizontalItemCount;
    private OnImageItemChangeListener mOnImageItemChangeListener;

    public LocalMultiImageAdapter(List<String> currentImagePathList, List<String> imageSelectedList, int horizontalItemCount) {
        mImagePathList = currentImagePathList;
        mImageSelectedList = imageSelectedList;
        mHorizontalItemCount = horizontalItemCount;
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        int itemSize = parent.getWidth() / mHorizontalItemCount;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_local_image, parent, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(itemSize, itemSize));
        return new ItemView(view, mProxyImageItemChangeListener);
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        holder.mCbSelected.setChecked(mImageSelectedList.contains(mImagePathList.get(position)));
        GlideUtil.loadFromUrl(mContext, holder.mIvImage, String.format("file://%s", mImagePathList.get(position)));
    }

    @Override
    public int getViewHolderCount() {
        return mImagePathList == null ? 0 : mImagePathList.size();
    }

    public void setOnImageItemChangeListener(OnImageItemChangeListener onImageItemChangeListener) {
        mOnImageItemChangeListener = onImageItemChangeListener;
    }

    private final OnImageItemChangeListener mProxyImageItemChangeListener = new OnImageItemChangeListener() {
        @Override
        public boolean onItemSelect(int position, boolean isSelect) {
            return mOnImageItemChangeListener == null || mOnImageItemChangeListener.onItemSelect(position, isSelect);
        }

        @Override
        public void onItemClick(int position) {
            if (mOnImageItemChangeListener != null) {
                mOnImageItemChangeListener.onItemClick(position);
            }
        }
    };


    final static class ItemView extends BaseRecyclerViewAdapter.BaseViewHolder {

        ImageView mIvImage;
        CheckBox mCbSelected;

        private ColorStateList mSelectColorTint;
        private ColorStateList mUnselectedColorTint;

        private OnImageItemChangeListener mOnImageItemChangeListener;

        ItemView(View itemView, OnImageItemChangeListener onImageItemChangeListener) {
            super(itemView);
            mOnImageItemChangeListener = onImageItemChangeListener;
            initView();
            setView();
        }

        private void initView() {
            mIvImage = itemView.findViewById(R.id.ImageSelectorActivity_mIvImage);
            mCbSelected = itemView.findViewById(R.id.ImageSelectorActivity_mCbSelected);

            mSelectColorTint = ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.mask_color_black));
            mUnselectedColorTint = ColorStateList.valueOf(Color.parseColor("#20000000"));
        }

        private void setView() {
            mCbSelected.setOnCheckedChangeListener(mOnCheckedChangeListener);
            mIvImage.setOnClickListener(mOnImageClickListener);
        }

        private final View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnImageItemChangeListener.onItemClick(getAdapterPosition());
            }
        };

        private final CheckBox.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mOnImageItemChangeListener.onItemSelect(ItemView.this.getAdapterPosition(), isChecked)) {
                    isChecked = !isChecked;
                    mCbSelected.setOnCheckedChangeListener(null);
                    mCbSelected.setChecked(isChecked);
                    mCbSelected.setOnCheckedChangeListener(mOnCheckedChangeListener);
                }
                mOnImageItemChangeListener.onItemSelect(ItemView.this.getAdapterPosition(), isChecked);
                if (isChecked) {
                    mIvImage.setImageTintList(mSelectColorTint);
                } else {
                    mIvImage.setImageTintList(mUnselectedColorTint);
                }
            }
        };


    }

    public interface OnImageItemChangeListener {
        boolean onItemSelect(int position, boolean isSelect);

        void onItemClick(int position);
    }

}
