package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Created by YZX on 2018年08月25日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class DirectoryPathAdapter extends BaseRecyclerViewAdapter<DirectoryPathAdapter.DirectoryPathHolder> {


    private List<String> mDirectoryNameList;

    public DirectoryPathAdapter(List<String> directoryNameList) {
        mDirectoryNameList = directoryNameList;
    }

    @Override
    public DirectoryPathAdapter.DirectoryPathHolder getViewHolder(ViewGroup parent, int viewType) {
        return new DirectoryPathHolder(LayoutInflater.from(mContext).inflate(R.layout.item_directory_path, parent, false));
    }

    @Override
    public void bindDataToViewHolder(DirectoryPathAdapter.DirectoryPathHolder holder, int position) {
        holder.mIvIcon.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        holder.mTvName.setText(mDirectoryNameList.get(position));
    }

    @Override
    public int getViewHolderCount() {
        return mDirectoryNameList == null ? 0 : mDirectoryNameList.size();
    }

    final static class DirectoryPathHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvName;
        ImageView mIvIcon;

        DirectoryPathHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.DirectoryPathAdapter_mTvName);
            mIvIcon = itemView.findViewById(R.id.DirectoryPathAdapter_mIvIcon);
        }
    }

}
