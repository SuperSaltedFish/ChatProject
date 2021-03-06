package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by YZX on 2017年06月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactAdapter extends BaseRecyclerViewAdapter<ContactAdapter.ContactHolder> {

    private SparseArray<String> mIdentitySparseArray;

    public ContactAdapter() {
        this(false);
    }

    public ContactAdapter(boolean isSearchMode) {
        mIdentitySparseArray = new SparseArray<>(32);
        if(!isSearchMode){
            registerAdapterDataObserver(mDataObserver);
        }
    }

    @Override
    public ContactHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ContactHolder holder, int position) {
        String identity = mIdentitySparseArray.get(position);
        if (identity != null) {
            holder.itemView.setTag(identity);
        } else {
            holder.itemView.setTag(null);
        }
        ContactEntity contactEntity = getItem(position);
        UserEntity user = contactEntity.getUserProfile();
        holder.mTvName.setText(contactEntity.getName());
        holder.mIvSex.setSelected(user.getSex() == UserEntity.SEX_WOMAN);
        holder.mTvAge.setText(user.getAge());

        if (TextUtils.isEmpty(user.getSignature())) {
            holder.mTvSignature.setText(null);
            holder.mTvSignature.setVisibility(View.GONE);
        } else {
            holder.mTvSignature.setText(user.getSignature());
            holder.mTvSignature.setVisibility(View.VISIBLE);
        }

        holder.mTagsFlowLayout.removeAllViews();
        ArrayList<String> tags = contactEntity.getTags();
        if (tags != null && tags.size() != 0) {
            TextView label;
            for (String tag : tags) {
                label = (TextView) LayoutInflater.from(mContext).inflate(R.layout.item_label_small, holder.mTagsFlowLayout, false);
                label.setText(tag);
                holder.mTagsFlowLayout.addView(label);
            }
        }
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, user.getAvatar());
    }

    @Override
    public void onViewHolderRecycled(ContactHolder holder) {
        GlideUtil.clear(mContext, holder.mIvAvatar);
    }

    @Override
    public int getViewHolderCount() {
        return mAsyncListDiffer.getCurrentList().size();
    }

    public void submitList(List<ContactEntity> contactList) {
        mAsyncListDiffer.submitList(contactList);
    }

    public List<ContactEntity> getContactList() {
        return mAsyncListDiffer.getCurrentList();
    }

    public ContactEntity getItem(int position) {
        return mAsyncListDiffer.getCurrentList().get(position);
    }

    public int findPositionByLetter(String letter) {
        int keyIndex = mIdentitySparseArray.indexOfValue(letter);
        if (keyIndex != -1) {
            return mIdentitySparseArray.keyAt(keyIndex);
        }
        return -1;
    }

    private void resetLetter() {
        mIdentitySparseArray.clear();
        List<ContactEntity> contactList = mAsyncListDiffer.getCurrentList();
        if (contactList.size() != 0) {
            String identity;
            String abbreviation;
            String currentIdentity = null;
            for (int i = 0, length = contactList.size(); i < length; i++) {
                abbreviation = contactList.get(i).getAbbreviation();
                if (!TextUtils.isEmpty(abbreviation)) {
                    identity = abbreviation.substring(0, 1);
                    if (!identity.equals(currentIdentity)) {
                        mIdentitySparseArray.append(i, identity.toUpperCase().intern());
                        currentIdentity = identity;
                    }
                }
            }
        }
    }

    private final AsyncListDiffer<ContactEntity> mAsyncListDiffer = new AsyncListDiffer<>(
            new BaseRecyclerViewAdapter.ListUpdateCallback(this),
            new AsyncDifferConfig.Builder<>(new DiffUtil.ItemCallback<ContactEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ContactEntity oldItem, @NonNull ContactEntity newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ContactEntity oldItem, @NonNull ContactEntity newItem) {
                    if (!oldItem.getName().equals(newItem.getName())) {
                        return false;
                    }
                    if (!oldItem.getUserProfile().getAvatar().equals(newItem.getUserProfile().getAvatar())) {
                        return false;
                    }
                    if (!oldItem.getUserProfile().getNickname().equals(newItem.getUserProfile().getNickname())) {
                        return false;
                    }
                    return true;
                }

            }).build());

    final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            resetLetter();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            resetLetter();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            resetLetter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            resetLetter();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            resetLetter();
        }

    };

    static class ContactHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        TextView mTvName;
        ImageView mIvAvatar;
        ImageView mIvSex;
        TextView mTvSignature;
        TextView mTvAge;
        FlowLayout mTagsFlowLayout;

        ContactHolder(View itemView) {
            super(itemView);
            initView();

        }

        private void initView() {
            mTvName = itemView.findViewById(R.id.mTvName);
            mIvAvatar = itemView.findViewById(R.id.mIvAvatar);
            mIvSex = itemView.findViewById(R.id.mIvSex);
            mTagsFlowLayout = itemView.findViewById(R.id.mTagsFlowLayout);
            mTvSignature = itemView.findViewById(R.id.mTvSignature);
            mTvAge = itemView.findViewById(R.id.mTvAge);
            mTagsFlowLayout.setMaxLine(1);
            mTagsFlowLayout.setItemSpace((int) AndroidHelper.dip2px(4));
        }

    }


}
