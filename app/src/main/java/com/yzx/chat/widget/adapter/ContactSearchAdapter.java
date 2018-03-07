package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactSearchAdapter extends BaseAdapter implements Filterable {

    private List<ContactBean> mFriendList;
    private Filter mFriendFilter;

    public ContactSearchAdapter(List<ContactBean> contactBeanList) {
        mFriendList = new ArrayList<>(contactBeanList.size() + 3);
        mFriendFilter = new FriendFilter(contactBeanList);
    }

    public ContactBean getFriendBeanByPosition(int position){
        if(position<mFriendList.size()){
            return mFriendList.get(position);
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        return mFriendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            convertView.setTag(new ItemViewHolder(convertView));
        }
        ItemViewHolder viewHolder = (ItemViewHolder) convertView.getTag();
        ContactBean contactBean = mFriendList.get(position);
        viewHolder.mTvName.setText(contactBean.getName());
        GlideUtil.loadCircleFromUrl(parent.getContext(), viewHolder.mIvHeadImage, R.drawable.temp_head_image);
        return convertView;
    }

    @Override
    public int getCount() {
        return mFriendList == null ? 0 : mFriendList.size();
    }

    @Override
    public Filter getFilter() {
        return mFriendFilter;
    }

    private static class ItemViewHolder {
        private View mItemView;

        TextView mTvName;
        ImageView mIvHeadImage;

        public ItemViewHolder(View itemView) {
            mItemView = itemView;
            initView();
        }

        private void initView() {
            mTvName = (TextView) mItemView.findViewById(R.id.ContactAdapter_mTvName);
            mIvHeadImage = (ImageView) mItemView.findViewById(R.id.ContactAdapter_mIvHeadImage);
            mItemView.setClickable(false);
        }
    }

    private class FriendFilter extends Filter {
        private List<ContactBean> mAllFriendList;
        private List<ContactBean> mFilterFriendList;
        private FilterResults mResults;

        FriendFilter(List<ContactBean> allFriendList) {
            mAllFriendList = allFriendList;
            mFilterFriendList = new ArrayList<>(allFriendList.size() + 3);
            mResults = new FilterResults();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mFilterFriendList.clear();
            if (constraint != null) {
                constraint = constraint.toString().toLowerCase();
                for (ContactBean friend : mAllFriendList) {
                    if (friend.getName().contains(constraint) || friend.getAbbreviation().contains(constraint)) {
                        mFilterFriendList.add(friend);
                    }
                }
            }
            mResults.values = mFilterFriendList;
            mResults.count = mFilterFriendList.size();
            return mResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFriendList.clear();
            mFriendList.addAll(mFilterFriendList);
            notifyDataSetChanged();
        }
    }


}
