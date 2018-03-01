package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public LocationAdapter(List<PoiItem> POIList) {
        mPOIList = POIList;
    }

    @Override
    public LocationSearchHolder getViewHolder(ViewGroup parent, int viewType) {
        return new LocationSearchHolder(LayoutInflater.from(mContext).inflate(R.layout.item_location,parent,false));
    }

    @Override
    public void bindDataToViewHolder(LocationSearchHolder holder, int position) {
        PoiItem poi = mPOIList.get(position);
        holder.mTvName.setText(poi.getTitle());
        holder.mTvAddress.setText(poi.getSnippet());
    }

    @Override
    public int getViewHolderCount() {
        return mPOIList == null ? 0 : mPOIList.size();
    }

    final static class LocationSearchHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvName;
        TextView mTvAddress;

        LocationSearchHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.LocationAdapter_mTvName);
            mTvAddress = itemView.findViewById(R.id.LocationAdapter_mTvAddress);
        }
    }
}
