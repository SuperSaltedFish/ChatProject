package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
        mIdentitySparseArray = new SparseArray<>(30);
        registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            AutoCompleteTextView view = new AutoCompleteTextView(parent.getContext());
            parent.addView(view);
            return new ItemSearchView(view, mFriendList);
        } else {
            return new ItemFriendView(LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        if (position != 0) {
            position--;
            ItemFriendView friendHolder = (ItemFriendView) holder;
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

    private static class ItemFriendView extends ItemView {

        TextView mTvName;
        ImageView mIvHeadImage;

        ItemFriendView(View itemView) {
            super(itemView);
            initView();

        }

        private void initView() {
            mTvName = (TextView) itemView.findViewById(R.id.FriendsAdapter_mTvName);
            mIvHeadImage = (ImageView) itemView.findViewById(R.id.FriendsAdapter_mIvHeadImage);
        }
    }


    private static class ItemSearchView extends ItemView {

        private List<FriendBean> mFriendList;
        private ContactSearchAdapter mSearchAdapter;

        AutoCompleteTextView mAutoCompleteTextView;

        ItemSearchView(View itemView, List<FriendBean> friendList) {
            super(itemView);
            mFriendList = friendList;
            initView();
            setView();

        }

        private void initView() {
            mAutoCompleteTextView = (AutoCompleteTextView) itemView;
            mSearchAdapter = new ContactSearchAdapter(mFriendList);
        }

        private void setView() {
            Context context = mAutoCompleteTextView.getContext();
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) AndroidTool.dip2px(40));
            params.setMargins((int) AndroidTool.dip2px(16), (int) AndroidTool.dip2px(8), (int) AndroidTool.dip2px(16), 0);
            mAutoCompleteTextView.setLayoutParams(params);
            mAutoCompleteTextView.setAdapter(mSearchAdapter);
            mAutoCompleteTextView.setThreshold(1);
            mAutoCompleteTextView.setHint("Search");
            mAutoCompleteTextView.setTextSize(14);
            mAutoCompleteTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_alpha_black));
            mAutoCompleteTextView.setBackground(null);
            mAutoCompleteTextView.setOnItemClickListener(mOnSearchItemClickListener);
            mAutoCompleteTextView.setDropDownVerticalOffset((int) AndroidTool.dip2px(8));
            mAutoCompleteTextView.setCompoundDrawablePadding((int) AndroidTool.dip2px(8));
            mAutoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_search), null, null, null);
        }

        private final AdapterView.OnItemClickListener mOnSearchItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendBean friendBean = mSearchAdapter.getFriendBeanByPosition(position);
                mAutoCompleteTextView.setText(friendBean.getName());
            }
        };
    }

}
