package com.yzx.chat.widget.adapter;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;

import java.util.List;

/**
 * Created by YZX on 2018年02月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class CreateGroupAdapter extends BaseRecyclerViewAdapter<CreateGroupAdapter.CreateGroupHolder> {

    private OnItemSelectedChangeListener mOnItemSelectedChangeListener;
    private List<ContactEntity> mContactList;
    private List<ContactEntity> mSelectedList;
    private List<ContactEntity> mAlreadySelectedContactList;
    private SparseArray<String> mIdentitySparseArray;


    public CreateGroupAdapter(List<ContactEntity> contactList, List<ContactEntity> selectedList) {
        mContactList = contactList;
        mSelectedList = selectedList;
        mIdentitySparseArray = new SparseArray<>(32);
        resetLetter();
    }

    @Override
    public CreateGroupHolder getViewHolder(ViewGroup parent, int viewType) {
        return new CreateGroupHolder(LayoutInflater.from(mContext).inflate(R.layout.item_create_group, parent, false), mItemSelectedChangeListenerProxy);
    }

    @Override
    public void bindDataToViewHolder(CreateGroupHolder holder, int position) {
        String identity = mIdentitySparseArray.get(position);
        if (identity != null) {
            holder.itemView.setTag(identity);
        } else {
            holder.itemView.setTag(null);
        }
        ContactEntity contact = mContactList.get(position);
        holder.mTvName.setText(contact.getName());
        if (mAlreadySelectedContactList != null && mAlreadySelectedContactList.contains(mContactList.get(position))) {
            holder.itemView.setEnabled(false);
            holder.mCbIsSelected.setChecked(true);
            holder.mCbIsSelected.setEnabled(false);
        } else {
            holder.itemView.setEnabled(true);
            holder.mCbIsSelected.setEnabled(true);
            holder.mCbIsSelected.setChecked(mSelectedList.contains(contact));
        }
        GlideUtil.loadAvatarFromUrl(mContext,holder.mIvAvatar,contact.getUserProfile().getAvatar());
    }

    @Override
    public int getViewHolderCount() {
        return mContactList == null ? 0 : mContactList.size();
    }

    public int findPositionByLetter(String letter) {
        int keyIndex = mIdentitySparseArray.indexOfValue(letter);
        if (keyIndex != -1) {
            return mIdentitySparseArray.keyAt(keyIndex);
        }
        return -1;
    }

    private void resetLetter() {
        if (mContactList != null && mContactList.size() != 0) {
            String identity;
            String abbreviation;
            String currentIdentity = null;
            for (int i = 0, length = mContactList.size(); i < length; i++) {
                abbreviation = mContactList.get(i).getAbbreviation();
                if (abbreviation != null) {
                    identity = abbreviation.substring(0, 1);
                    if (!identity.equals(currentIdentity)) {
                        mIdentitySparseArray.append(i, identity.toUpperCase().intern());
                        currentIdentity = identity;
                    }
                }
            }
        }
    }


    public void setOnItemSelectedChangeListener(OnItemSelectedChangeListener onItemSelectedChangeListener) {
        mOnItemSelectedChangeListener = onItemSelectedChangeListener;
    }

    private final OnItemSelectedChangeListener mItemSelectedChangeListenerProxy = new OnItemSelectedChangeListener() {
        @Override
        public void onItemSelectedChange(int position, boolean isSelect) {
            if (mOnItemSelectedChangeListener != null) {
                mOnItemSelectedChangeListener.onItemSelectedChange(position, isSelect);
            }
        }
    };

    public void setDisableSelectedList(List<ContactEntity> alreadySelectedContactList) {
        mAlreadySelectedContactList = alreadySelectedContactList;
    }

    final static class CreateGroupHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        private OnItemSelectedChangeListener mOnItemSelectedChangeListener;
        ImageView mIvAvatar;
        TextView mTvName;
        CheckBox mCbIsSelected;

        CreateGroupHolder(View itemView, OnItemSelectedChangeListener onItemSelectedChangeListener) {
            super(itemView);
            mOnItemSelectedChangeListener = onItemSelectedChangeListener;
            mIvAvatar = itemView.findViewById(R.id.CreateGroupAdapter_mIvAvatar);
            mCbIsSelected = itemView.findViewById(R.id.CreateGroupAdapter_mCbIsSelected);
            mTvName = itemView.findViewById(R.id.CreateGroupAdapter_mTvName);
            itemView.setOnClickListener(mOnItemClickListener);
        }

        private final View.OnClickListener mOnItemClickListener = new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mCbIsSelected.setChecked(!mCbIsSelected.isChecked());
                mOnItemSelectedChangeListener.onItemSelectedChange(getAdapterPosition(), mCbIsSelected.isChecked());
            }
        };

    }

    public interface OnItemSelectedChangeListener {
        void onItemSelectedChange(int position, boolean isSelect);
    }
}
