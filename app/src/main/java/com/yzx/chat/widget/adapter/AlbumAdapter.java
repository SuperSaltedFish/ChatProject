package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.widget.view.NineGridImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AlbumAdapter extends BaseRecyclerViewAdapter<AlbumAdapter.AlbumHolder> {


    @Override
    public AlbumHolder getViewHolder(ViewGroup parent, int viewType) {
        return new AlbumHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album,parent,false));
    }

    @Override
    public void bindDataToViewHolder(AlbumHolder holder, int position) {
        List<String> s = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            s.add(new String("" + i));
        }
        if(s.size()==0){
            holder.mNineGridImageView.setVisibility(View.GONE);
            holder.mDivisionLine.setVisibility(View.INVISIBLE);
        }else {
            holder.mNineGridImageView.setVisibility(View.VISIBLE);
            holder.mDivisionLine.setVisibility(View.VISIBLE);
        }
        holder.mNineGridImageView.setImageData(s);
        holder.mNineGridImageView.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public int getViewHolderCount() {
        return 10;
    }


    private final NineGridImageView.OnItemClickListener mOnItemClickListener = new NineGridImageView.OnItemClickListener() {
        @Override
        public void onItemClick(ImageView view, int position, Object imageUri) {

        }
    };

    final static class AlbumHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        NineGridImageView mNineGridImageView;
        View mDivisionLine;

        AlbumHolder(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            mNineGridImageView = (NineGridImageView) itemView.findViewById(R.id.AlbumAdapter_mNineGridImageView);
            mDivisionLine = itemView.findViewById(R.id.AlbumAdapter_mDivisionLine);
        }

    }
}
