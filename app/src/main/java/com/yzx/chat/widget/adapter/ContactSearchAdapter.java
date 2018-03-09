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
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactSearchAdapter extends BaseRecyclerViewAdapter<ContactSearchAdapter.ContactHolder> {

    private List<ContactBean> mContactList;
    private List<ContactBean> mSearchContactList;

    public ContactSearchAdapter(List<ContactBean> contactList,List<ContactBean> searchContactList) {
        mContactList = contactList;
        mSearchContactList = searchContactList;
    }

    @Override
    public ContactHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ContactHolder holder, int position) {
        ContactBean contactBean = mSearchContactList.get(position);
        holder.mTvName.setText(contactBean.getName());
        GlideUtil.loadFromUrl(mContext, holder.mIvHeadImage, R.drawable.temp_head_image);
    }

    @Override
    public int getViewHolderCount() {
        return mSearchContactList == null ? 0 : mSearchContactList.size();
    }


    static  class ContactHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        TextView mTvName;
        ImageView mIvHeadImage;

        ContactHolder(View itemView) {
            super(itemView);
            initView();

        }

        private void initView() {
            mTvName = itemView.findViewById(R.id.ContactAdapter_mTvName);
            mIvHeadImage = itemView.findViewById(R.id.ContactAdapter_mIvHeadImage);
        }
    }


}
