package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactSearchAdapter extends BaseRecyclerViewAdapter<ContactSearchAdapter.ContactHolder> {

    private List<ContactEntity> mContactList;
    private List<ContactEntity> mSearchContactList;

    public ContactSearchAdapter( List<ContactEntity> searchContactList) {
        mSearchContactList = searchContactList;
    }

    @Override
    public ContactHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ContactHolder holder, int position) {
        ContactEntity contactEntity = mSearchContactList.get(position);
        holder.mTvName.setText(contactEntity.getName());
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, contactEntity.getAvatar());
    }

    @Override
    public void onViewHolderRecycled(ContactHolder holder) {
        GlideUtil.clear(mContext,holder.mIvAvatar);
    }

    @Override
    public int getViewHolderCount() {
        return mSearchContactList == null ? 0 : mSearchContactList.size();
    }

    public void setContactList(List<ContactEntity> contactList) {
        mContactList = contactList;
        notifyDataSetChanged();
    }

    public int setFilterText(String text) {
        if (mContactList != null && mSearchContactList != null) {
            mSearchContactList.clear();
            if (!TextUtils.isEmpty(text)) {
                text = text.toLowerCase();
                for (ContactEntity contact : mContactList) {
                    if (contact.getName().contains(text) || contact.getAbbreviation().contains(text)) {
                        mSearchContactList.add(contact);
                    }
                }
            }
            notifyDataSetChanged();
            return mSearchContactList.size();
        }
        return 0;
    }


    static class ContactHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        TextView mTvName;
        ImageView mIvAvatar;

        ContactHolder(View itemView) {
            super(itemView);
            initView();

        }

        private void initView() {
            mTvName = itemView.findViewById(R.id.mTvName);
            mIvAvatar = itemView.findViewById(R.id.mIvAvatar);
        }
    }


}
