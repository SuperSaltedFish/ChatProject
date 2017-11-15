package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.FriendBean;
import com.yzx.chat.tool.AndroidTool;
import com.yzx.chat.util.GlideUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by YZX on 2017年06月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactAdapter extends BaseRecyclerViewAdapter<ContactAdapter.ItemView> {

    private List<FriendBean> mFriendList;
    private SparseArray<String> mIdentitySparseArray;

    public ContactAdapter(List<FriendBean> friendList) {
        mFriendList = friendList;
        mIdentitySparseArray = new SparseArray<>(32);
        registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ItemSearchView(LayoutInflater.from(mContext).inflate(R.layout.item_contact_search, parent, false), mFriendList);
        } else {
            return new ItemContactView(LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        if (position == 0) {

        }else {
            position--;
            ItemContactView friendHolder = (ItemContactView) holder;
            String identity = mIdentitySparseArray.get(position);
            if (identity != null) {
                holder.itemView.setTag(identity);
            } else {
                holder.itemView.setTag(null);
            }
            FriendBean friendBean = mFriendList.get(position);
            friendHolder.mTvName.setText(friendBean.getName());
            GlideUtil.loadCircleFromUrl(mContext, friendHolder.mIvHeadImage, R.drawable.temp_head_image);
        }
    }

    @Override
    public void onViewRecycled(ItemView holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mFriendList == null ? 0 : mFriendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public int findPositionByLetter(String letter) {
        int keyIndex = mIdentitySparseArray.indexOfValue(letter);
        if (keyIndex != -1) {
            return mIdentitySparseArray.keyAt(keyIndex);
        }
        return -1;
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (mFriendList != null) {
                Collections.sort(mFriendList, mFriendNameComparator);
                String identity;
                String abbreviation;
                String currentIdentity = null;
                for (int i = 0, length = mFriendList.size(); i < length; i++) {
                    abbreviation = mFriendList.get(i).getNameAbbreviation();
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

    };

    private final Comparator<FriendBean> mFriendNameComparator = new Comparator<FriendBean>() {
        @Override
        public int compare(FriendBean o1, FriendBean o2) {
            if (o2 != null && o1 != null) {
                return o1.getNameAbbreviation().compareTo(o2.getNameAbbreviation());
            } else {
                return 0;
            }
        }
    };

    static abstract class ItemView extends RecyclerView.ViewHolder {

        ItemView(View itemView) {
            super(itemView);
        }
    }

    private static class ItemContactView extends ItemView {

        TextView mTvName;
        ImageView mIvHeadImage;

        ItemContactView(View itemView) {
            super(itemView);
            initView();

        }

        private void initView() {
            mTvName = (TextView) itemView.findViewById(R.id.ContactAdapter_mTvName);
            mIvHeadImage = (ImageView) itemView.findViewById(R.id.ContactAdapter_mIvHeadImage);
        }
    }


    private static class ItemSearchView extends ItemView {

        private List<FriendBean> mFriendList;
        private ContactSearchAdapter mSearchAdapter;

        AutoCompleteTextView mSearchView;

        ItemSearchView(View itemView, List<FriendBean> friendList) {
            super(itemView);
            mFriendList = friendList;
            initView();
            setView();

        }

        private void initView() {
            mSearchView = (AutoCompleteTextView) itemView.findViewById(R.id.ContactAdapter_mSearchView);
            mSearchAdapter = new ContactSearchAdapter(mFriendList);
        }

        private void setView() {
            mSearchView.setAdapter(mSearchAdapter);
            mSearchView.setOnItemClickListener(mOnSearchItemClickListener);
            mSearchView.setDropDownVerticalOffset((int) AndroidTool.dip2px(8));
        }

        private final AdapterView.OnItemClickListener mOnSearchItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendBean friendBean = mSearchAdapter.getFriendBeanByPosition(position);
                mSearchView.setText(friendBean.getName());
            }
        };
    }

}
