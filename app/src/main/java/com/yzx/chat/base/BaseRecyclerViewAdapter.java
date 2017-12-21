package com.yzx.chat.base;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by YZX on 2017年08月20日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public Context mContext;

    private OnScrollToBottomListener mScrollToBottomListener;

    public abstract VH getViewHolder(ViewGroup parent, int viewType);

    public abstract void bindDataToViewHolder(VH holder, int position);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        return getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        bindDataToViewHolder(holder, position);
        if (mScrollToBottomListener != null&&position==getItemCount()-1) {
            holder.itemView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollToBottomListener.OnScrollToBottom();
                }
            });
        }
    }

    public void setScrollToBottomListener(OnScrollToBottomListener listener) {
        mScrollToBottomListener = listener;
    }

    public  interface OnScrollToBottomListener {
        void OnScrollToBottom();
    }



}
