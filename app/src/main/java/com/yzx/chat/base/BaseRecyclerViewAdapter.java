package com.yzx.chat.base;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年08月20日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewAdapter.BaseViewHolder>
        extends RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder> {

    private static final int HOLDER_TYPE_HEADER = -1;

    public Context mContext;

    private View mHeaderView;

    private OnScrollToBottomListener mScrollToBottomListener;

    private int mLastBindPosition;

    public abstract VH getViewHolder(ViewGroup parent, int viewType);

    public abstract void bindDataToViewHolder(VH holder, int position);

    public abstract int getViewHolderCount();

    public  int getViewHolderType(int position){
        return super.getItemViewType(position);
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        if (viewType == HOLDER_TYPE_HEADER) {
            return new BaseViewHolder(mHeaderView);
        } else {
            return getViewHolder(parent, viewType);
        }
    }

    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        if(mHeaderView!=null){
            if(position!=0){
                bindDataToViewHolder((VH) holder, position-1);
            }
        }else {
            bindDataToViewHolder((VH) holder, position);
        }

        if (mScrollToBottomListener != null&&position==getItemCount()-1&&position!=mLastBindPosition) {
            holder.itemView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollToBottomListener.OnScrollToBottom();
                }
            });
        }
        mLastBindPosition = position;
    }

    @Override
    public final int getItemCount() {
        int count = getViewHolderCount();
        if (mHeaderView == null) {
            return count;
        } else {
            return count + 1;
        }
    }

    @Override
    public final int getItemViewType(int position) {
        if (mHeaderView == null) {
            return getViewHolderType(position);
        } else if (position == 0) {
            return HOLDER_TYPE_HEADER;
        } else {
            return getViewHolderType(position - 1);
        }
    }

    public void addHeaderView(View headerView) {
        mHeaderView = headerView;
    }

    public void setScrollToBottomListener(OnScrollToBottomListener listener) {
        mScrollToBottomListener = listener;
    }

    public final void notifyItemRangeInsertedEx(int positionStart, int itemCount) {
        this.notifyItemRangeInserted(positionStart, itemCount);
    }

    public final void notifyItemRangeRemovedEx(int positionStart, int itemCount) {
        this.notifyItemRangeRemoved(positionStart, itemCount);
    }

    public final void notifyItemMovedEx(int fromPosition, int toPosition) {
        this.notifyItemMoved(fromPosition, toPosition);
    }

    public final void notifyItemRangeChangedEx(int positionStart, int itemCount, Object payload) {
        this.notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    public interface OnScrollToBottomListener {
        void OnScrollToBottom();
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }


}
