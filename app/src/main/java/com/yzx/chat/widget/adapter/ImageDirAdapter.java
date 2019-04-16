package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.GlideUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by YZX on 2017年08月19日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ImageDirAdapter extends BaseRecyclerViewAdapter<ImageDirAdapter.ItemView> {

    private List<String> mImageDirPath;
    private Map<String, List<String>> mGroupingMap;
    private int mSelectedPosition;

    public ImageDirAdapter(List<String> imageDirPath, Map<String, List<String>> groupingMap) {
        mImageDirPath = imageDirPath;
        mGroupingMap = groupingMap;
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        return new ItemView(LayoutInflater.from(mContext).inflate(R.layout.item_image_dir, parent, false));
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        if (mSelectedPosition == position) {
            holder.mIvSelectedIcon.setVisibility(View.VISIBLE);
            holder.mTvDirPath.setTextColor(AndroidHelper.getColor(R.color.colorAccent));
        } else {
            holder.mIvSelectedIcon.setVisibility(View.INVISIBLE);
            holder.mTvDirPath.setTextColor(AndroidHelper.getColor(R.color.textColorPrimary));
        }
        if (position == 0) {
            holder.mTvDirPath.setText(R.string.ImageSelectorActivity_AllImage);
        } else {
            position--;
            String fileDirPath = mImageDirPath.get(position);
            List<String> imagePathList = mGroupingMap.get(fileDirPath);
            if (imagePathList != null && imagePathList.size() != 0) {
                GlideUtil.loadFromUrl(mContext, holder.mIvLowSource, imagePathList.get(0));
                holder.mTvDirPath.setText(String.format(Locale.getDefault(), "%s(%d)", fileDirPath.substring(fileDirPath.lastIndexOf("/") + 1), imagePathList.size()));
            }
        }
    }

    @Override
    public void onViewHolderRecycled(ItemView holder) {
        GlideUtil.clear(mContext, holder.mIvLowSource);
    }

    @Override
    public int getViewHolderCount() {
        return mImageDirPath == null ? 1 : mImageDirPath.size() + 1;
    }

    public void setSelectedPosition(int position) {
        if (mSelectedPosition != position) {
            mSelectedPosition = position;
            notifyDataSetChanged();
        }
    }

    final static class ItemView extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvDirPath;
        ImageView mIvLowSource;
        ImageView mIvSelectedIcon;

        ItemView(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            mTvDirPath = itemView.findViewById(R.id.ImageDirAdapter_mTvDirPath);
            mIvLowSource = itemView.findViewById(R.id.ImageDirAdapter_mIvLowSource);
            mIvSelectedIcon = itemView.findViewById(R.id.ImageDirAdapter_mIvSelectedIcon);
        }
    }
}
