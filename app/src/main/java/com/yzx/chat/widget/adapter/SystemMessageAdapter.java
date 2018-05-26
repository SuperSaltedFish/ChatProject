package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

/**
 * Created by YZX on 2018年05月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class SystemMessageAdapter extends BaseRecyclerViewAdapter<SystemMessageAdapter.SystemMessageHolder> {

    @Override
    public SystemMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SystemMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_system_message, parent, false));
    }

    @Override
    public void bindDataToViewHolder(SystemMessageHolder holder, int position) {

    }

    @Override
    public int getViewHolderCount() {
        return 16;
    }

    static final class SystemMessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        SystemMessageHolder(View itemView) {
            super(itemView);

        }
    }
}
