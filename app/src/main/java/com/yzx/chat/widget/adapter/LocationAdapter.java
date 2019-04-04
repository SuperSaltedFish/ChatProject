package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidHelper;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class LocationAdapter extends BaseRecyclerViewAdapter<LocationAdapter.LocationSearchHolder> {

    private List<PoiItem> mPOIList;
    private int mSelectedPosition;
    private int mSelectedTextColor;
    private int mUnselectedTextColor;

    public LocationAdapter(List<PoiItem> POIList) {
        mPOIList = POIList;
        mSelectedTextColor = AndroidHelper.getColor(R.color.colorAccent);
        mUnselectedTextColor = AndroidHelper.getColor(R.color.textColorPrimary);
    }

    @Override
    public LocationSearchHolder getViewHolder(ViewGroup parent, int viewType) {
        return new LocationSearchHolder(LayoutInflater.from(mContext).inflate(R.layout.item_location, parent, false));
    }

    @Override
    public void bindDataToViewHolder(LocationSearchHolder holder, int position) {
        PoiItem poi = mPOIList.get(position);
        holder.mTvName.setText(poi.getTitle());
        holder.mTvAddress.setText(poi.getSnippet());
        if (position == mSelectedPosition) {
            holder.mTvName.setTextColor(mSelectedTextColor);
            holder.mTvAddress.setTextColor(mSelectedTextColor);
            holder.mIvSelectedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mTvName.setTextColor(mUnselectedTextColor);
            holder.mTvAddress.setTextColor(mUnselectedTextColor);
            holder.mIvSelectedIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getViewHolderCount() {
        return mPOIList == null ? 0 : mPOIList.size();
    }

    public void setSelectedPosition(int selectedPosition) {
        if (mSelectedPosition == selectedPosition) {
            return;
        }
        if (mSelectedPosition < getViewHolderCount()) {
            notifyItemChangedEx(mSelectedPosition);
        }
        if (selectedPosition < getViewHolderCount()) {
            notifyItemChangedEx(selectedPosition);
        }
        mSelectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    final static class LocationSearchHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvName;
        TextView mTvAddress;
        ImageView mIvSelectedIcon;

        LocationSearchHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.LocationAdapter_mTvName);
            mTvAddress = itemView.findViewById(R.id.LocationAdapter_mTvAddress);
            mIvSelectedIcon = itemView.findViewById(R.id.LocationAdapter_mIvSelectedIcon);
        }

        public void setSelected(boolean isSelected) {
            mIvSelectedIcon.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
