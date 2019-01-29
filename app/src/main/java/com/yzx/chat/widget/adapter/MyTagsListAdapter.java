package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.TagEntity;

import java.util.List;
import java.util.Locale;

/**
 * Created by YZX on 2018年06月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MyTagsListAdapter extends BaseRecyclerViewAdapter<MyTagsListAdapter.MyTagsListHolder> {

    private List<TagEntity> mTagList;

    public MyTagsListAdapter(List<TagEntity> tagList) {
        mTagList = tagList;
    }

    @Override
    public MyTagsListHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MyTagsListHolder(LayoutInflater.from(mContext).inflate(R.layout.item_my_tag_list, parent, false));
    }

    @Override
    public void bindDataToViewHolder(MyTagsListHolder holder, int position) {
        TagEntity tag = mTagList.get(position);
        holder.mTvItemTitle.setText(String.format(Locale.getDefault(), "%s(%d)", tag.getName(), tag.getMemberCount()));
    }

    @Override
    public int getViewHolderCount() {
        return mTagList == null ? 0 : mTagList.size();
    }

    static final class MyTagsListHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        TextView mTvItemTitle;

        MyTagsListHolder(View itemView) {
            super(itemView);
            mTvItemTitle = (TextView) itemView;
        }
    }
}
