package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class LocationAdapter extends BaseRecyclerViewAdapter<LocationAdapter.LocationSearchHolder> {

    private List<PoiItem> mPOIList;
    private int mSelectedPosition;

    public LocationAdapter(List<PoiItem> POIList) {
        mPOIList = POIList;
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
        holder.mRBtnSelected.setVisibility(position == mSelectedPosition ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getViewHolderCount() {
        return mPOIList == null ? 0 : mPOIList.size();
    }

    public void setSelectedPosition(int selectedPosition) {
        if (mSelectedPosition == selectedPosition) {
            return;
        }
        notifyItemChangedEx(mSelectedPosition);
        notifyItemChangedEx(selectedPosition);
        mSelectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    final static class LocationSearchHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvName;
        TextView mTvAddress;
        RadioButton mRBtnSelected;

        LocationSearchHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.LocationAdapter_mTvName);
            mTvAddress = itemView.findViewById(R.id.LocationAdapter_mTvAddress);
            mRBtnSelected = itemView.findViewById(R.id.LocationAdapter_mRBtnSelected);
        }

        public void setSelected(boolean isSelected) {
            mRBtnSelected.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
