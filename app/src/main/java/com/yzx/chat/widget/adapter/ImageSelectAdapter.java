package com.yzx.chat.widget.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
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

public class ImageSelectAdapter extends BaseRecyclerViewAdapter<ImageSelectAdapter.ItemView> {

    private List<String> mImagePathList;
    private int mHorizontalItemCount;
    private SparseBooleanArray mCheckedStateArray;
    private OnSelectedChangeListener mOnSelectedChangeListener;

    public ImageSelectAdapter(List<String> imagePathList, int horizontalItemCount) {
        mImagePathList = imagePathList;
        mHorizontalItemCount = horizontalItemCount;
        mCheckedStateArray = new SparseBooleanArray(128);
        registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        int itemSize = parent.getWidth() / mHorizontalItemCount;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_select, parent, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(itemSize, itemSize));
        return  new ItemView(view);
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        holder.mCbSelected.setChecked(mCheckedStateArray.get(position));
        GlideUtil.loadFromUrl(mContext, holder.mIvImage, String.format("file://%s", mImagePathList.get(position)));
    }


    @Override
    public void onViewRecycled(ItemView holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mImagePathList == null ? 0 : mImagePathList.size();
    }

    private void onSelectChange(int position, boolean isSelect) {
        mCheckedStateArray.put(position, isSelect);
        if (mOnSelectedChangeListener != null) {
            mOnSelectedChangeListener.onSelect(position, isSelect);
        }
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener onSelectedChangeListener) {
        mOnSelectedChangeListener = onSelectedChangeListener;
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mCheckedStateArray.clear();
        }
    };


    final class ItemView extends RecyclerView.ViewHolder {

        ImageView mIvImage;
        CheckBox mCbSelected;

        private ColorStateList mSelectColorTint;
        private ColorStateList mUnselectedColorTint;

        ItemView(View itemView) {
            super(itemView);
            initView();
            setView();
        }

        private void initView() {
            mIvImage = (ImageView) itemView.findViewById(R.id.ImageSelectorActivity_mIvImage);
            mCbSelected = (CheckBox) itemView.findViewById(R.id.ImageSelectorActivity_mCbSelected);

            mSelectColorTint = ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.mask_color_black));
            mUnselectedColorTint = ColorStateList.valueOf(Color.parseColor("#20000000"));
        }

        private void setView() {
            mCbSelected.setOnCheckedChangeListener(mOnCheckedChangeListener);
        }


        private final CheckBox.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onSelectChange(ItemView.this.getAdapterPosition(), isChecked);
                if (isChecked) {
                    mIvImage.setImageTintList(mSelectColorTint);
                } else {
                    mIvImage.setImageTintList(mUnselectedColorTint);
                }
            }
        };


    }

    public interface OnSelectedChangeListener {
        void onSelect(int position, boolean isSelect);
    }

}
