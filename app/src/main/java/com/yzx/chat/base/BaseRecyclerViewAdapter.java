package com.yzx.chat.base;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YZX on 2017年08月20日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewAdapter.BaseViewHolder>
        extends RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder> {

    private static final int DEFAULT_HOLDER_TYPE_HEADER = -1;
    private static final int DEFAULT_HOLDER_TYPE_FOOTER = -2;

    public Context mContext;
    private View mHeaderView;
    private View mFooterView;
    private OnScrollToBottomListener mScrollToBottomListener;
    private int mLastBindPosition;

    public abstract VH getViewHolder(ViewGroup parent, int viewType);

    public abstract void bindDataToViewHolder(VH holder, int position);

    public abstract int getViewHolderCount();

    public int getViewHolderType(int position) {
        return super.getItemViewType(position);
    }

    public int getHeaderViewHolderType() {
        return DEFAULT_HOLDER_TYPE_HEADER;
    }

    public int getFooterViewHolderType() {
        return DEFAULT_HOLDER_TYPE_FOOTER;
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        if (viewType == DEFAULT_HOLDER_TYPE_HEADER) {
            return new BaseViewHolder(mHeaderView);
        }
        if (viewType == DEFAULT_HOLDER_TYPE_FOOTER) {
            return new BaseViewHolder(mFooterView);
        }
        return getViewHolder(parent, viewType);
    }

    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        if (mContext == null) {
            mContext = holder.itemView.getContext();
        }
        if (mHeaderView != null) {
            if (position != 0) {
                bindDataToViewHolder((VH) holder, position - 1);
            }
        } else if (mFooterView == null || position != getItemCount() - 1) {
            bindDataToViewHolder((VH) holder, position);
        }

        if (mScrollToBottomListener != null && position == getItemCount() - 1 && position != mLastBindPosition) {
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
        if (mHeaderView != null) {
            count++;
        }
        if (mFooterView != null) {
            count++;
        }
        return count;
    }

    @Override
    public final int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return DEFAULT_HOLDER_TYPE_HEADER;
        }
        if (mFooterView != null && position == getItemCount() - 1) {
            return DEFAULT_HOLDER_TYPE_FOOTER;
        }
        if (mHeaderView != null) {
            return getViewHolderType(position - 1);
        } else {
            return getViewHolderType(position);
        }
    }

    public void setHeaderView(View headerView) {
        if (mHeaderView == null && headerView != null) {
            notifyItemInserted(0);
        } else if (mHeaderView != null && headerView == null) {
            notifyItemRemoved(0);
        } else if (mHeaderView != null && headerView != null && mHeaderView != headerView) {
            notifyItemChanged(0);
        }
        mHeaderView = headerView;
    }

    public boolean isHasHeaderView() {
        return mHeaderView != null;
    }

    public void setFooterView(View footerView) {
        if (mFooterView == null && footerView != null) {
            notifyItemInserted(getItemCount());
        } else if (mFooterView != null && footerView == null) {
            notifyItemRemoved(getItemCount());
        } else if (mFooterView != null && footerView != null && mFooterView != footerView) {
            notifyItemChanged(getItemCount());
        }
        mFooterView = footerView;
    }

    public boolean isHasFooterView() {
        return mFooterView != null;
    }

    public void setScrollToBottomListener(OnScrollToBottomListener listener) {
        mScrollToBottomListener = listener;
    }

    public final void notifyItemRangeInsertedEx(int positionStart, int itemCount) {
        if (mHeaderView == null) {
            this.notifyItemRangeInserted(positionStart, itemCount);
        } else {
            this.notifyItemRangeInserted(positionStart + 1, itemCount);
        }
    }

    public final void notifyItemRangeRemovedEx(int positionStart, int itemCount) {
        if (mHeaderView == null) {
            this.notifyItemRangeRemoved(positionStart, itemCount);
        } else {
            this.notifyItemRangeRemoved(positionStart + 1, itemCount);
        }
    }

    public final void notifyItemMovedEx(int fromPosition, int toPosition) {
        if (mHeaderView == null) {
            this.notifyItemMoved(fromPosition, toPosition);
        } else {
            this.notifyItemMoved(fromPosition + 1, toPosition);
        }
    }

    public final void notifyItemRangeChangedEx(int positionStart, int itemCount) {
        if (mHeaderView == null) {
            this.notifyItemRangeChanged(positionStart, itemCount);
        } else {
            this.notifyItemRangeChanged(positionStart + 1, itemCount);
        }
    }

    public final void notifyItemRangeChangedEx(int positionStart, int itemCount, Object payload) {
        if (mHeaderView == null) {
            this.notifyItemRangeChanged(positionStart, itemCount, payload);
        } else {
            this.notifyItemRangeChanged(positionStart + 1, itemCount, payload);
        }
    }

    public final void notifyItemInsertedEx(int position) {
        if (mHeaderView == null) {
            this.notifyItemInserted(position);
        } else {
            this.notifyItemInserted(position + 1);
        }
    }

    public final void notifyItemChangedEx(int position) {
        if (mHeaderView == null) {
            this.notifyItemChanged(position);
        } else {
            this.notifyItemChanged(position + 1);
        }
    }

    public final void notifyItemRemovedEx(int position) {
        if (mHeaderView == null) {
            this.notifyItemRemoved(position);
        } else {
            this.notifyItemRemoved(position + 1);
        }
    }

    public interface OnScrollToBottomListener {
        void OnScrollToBottom();
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ListUpdateCallback implements android.support.v7.util.ListUpdateCallback {

        private BaseRecyclerViewAdapter mAdapter;

        public ListUpdateCallback(BaseRecyclerViewAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onInserted(int position, int count) {
            mAdapter.notifyItemRangeInsertedEx(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            mAdapter.notifyItemRangeRemovedEx(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            mAdapter.notifyItemMovedEx(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            mAdapter.notifyItemRangeChangedEx(position, count, payload);
        }
    }


}
