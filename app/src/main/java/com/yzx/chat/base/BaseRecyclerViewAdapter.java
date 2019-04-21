package com.yzx.chat.base;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2018年08月20日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

@SuppressWarnings("unchecked")
public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewAdapter.BaseViewHolder>
        extends RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder> {

    private static final int HOLDER_TYPE_HEADER = -1;
    private static final int HOLDER_TYPE_FOOTER = -2;

    protected Context mContext;
    private View mHeaderView;
    private View mFooterView;
    private OnScrollToBottomListener mScrollToBottomListener;
    private int mLastBindPosition;

    public abstract VH getViewHolder(ViewGroup parent, int viewType);

    public abstract void bindDataToViewHolder(VH holder, int position);

    public abstract int getViewHolderCount();

    public void onViewHolderRecycled(VH holder) {
    }

    public int getViewHolderType(int position) {
        return super.getItemViewType(position);
    }

    public int getHeaderViewHolderType() {
        return HOLDER_TYPE_HEADER;
    }

    public int getFooterViewHolderType() {
        return HOLDER_TYPE_FOOTER;
    }

    @NonNull
    @Override
    public final BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        if (viewType == HOLDER_TYPE_HEADER) {
            return new HeaderHolder(mHeaderView);
        }
        if (viewType == HOLDER_TYPE_FOOTER) {
            return new FooterHolder(mFooterView);
        }
        return getViewHolder(parent, viewType);
    }

    @Override
    public final void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if (mContext == null) {
            mContext = holder.itemView.getContext();
        }
        if (holder instanceof HeaderHolder || holder instanceof FooterHolder) {
            return;
        }
        if (mHeaderView != null) {
            bindDataToViewHolder((VH) holder, position - 1);
        } else {
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
    public final void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof HeaderHolder || holder instanceof FooterHolder) {
            return;
        }
        onViewHolderRecycled((VH) holder);
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
            return HOLDER_TYPE_HEADER;
        }
        if (mFooterView != null && position == getItemCount() - 1) {
            return HOLDER_TYPE_FOOTER;
        }
        if (mHeaderView != null) {
            return getViewHolderType(position - 1);
        } else {
            return getViewHolderType(position);
        }
    }

    public void setHeaderView(View headerView) {
        if(mHeaderView!=headerView){
            notifyDataSetChanged();
        }
        mHeaderView = headerView;
    }

    public boolean isHasHeaderView() {
        return mHeaderView != null;
    }

    public void setFooterView(View footerView) {
        if(mFooterView!=footerView){
            notifyDataSetChanged();
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

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class HeaderHolder extends BaseViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FooterHolder extends BaseViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ListUpdateCallback implements androidx.recyclerview.widget.ListUpdateCallback {

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

    public interface OnScrollToBottomListener {
        void OnScrollToBottom();
    }
}
