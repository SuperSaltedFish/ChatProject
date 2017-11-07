package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.widget.view.HeadPortraitImageView;

import java.util.List;

import static com.yzx.chat.bean.ConversationBean.GROUP;
import static com.yzx.chat.bean.ConversationBean.SINGLE;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ItemView> {

    private static final int CHAT = 1;
    private static final int GROUPCHAT = 2;

    private List<EMConversation> mConversationList;

    public ConversationAdapter(List<EMConversation> conversationList) {
        mConversationList = conversationList;
    }


    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SINGLE) {
            return new SingleView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_single, parent, false));
        } else {
            return new GroupView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_group, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        EMConversation conversation = mConversationList.get(position);
        switch (conversation.getType()){
            case Chat:
                SingleView singleView = (SingleView) holder;
                singleView.mTvName.setText(single.getName());
                singleView.mTvLastRecord.setText(single.getLastMessage());
                singleView.mTvTime.setText(single.getTime());
                singleView.mHeadPortraitImageView.setStateEnabled(true);
                singleView.itemView.setTag(single);
                break;
            case GroupChat:
                GroupView groupView = (GroupView) holder;
                groupView.mTvName.setText(group.getName());
                groupView.mTvLastRecord.setText(group.getLastMessage());
                groupView.mTvTime.setText(group.getTime());
                groupView.mHeadPortraitImageView.setStateEnabled(true);
                groupView.itemView.setTag(group);
                break;
        }
    }


    @Override
    public void onBindViewHolder(ItemView holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onViewRecycled(ItemView holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        if (mConversationList == null) {
            return 0;
        }
        return mConversationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ConversationBean conversation = mConversationList.get(position);
        return conversation.getConversationMode();
    }

    static abstract class ItemView extends RecyclerView.ViewHolder {

        private int mConversationMode;

        ItemView(View itemView, int conversationMode) {
            super(itemView);
            mConversationMode = conversationMode;

        }

        public int getConversationMode() {
            return mConversationMode;
        }

    }

    private final static class SingleView extends ItemView {
        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;
        HeadPortraitImageView mHeadPortraitImageView;

        SingleView(View itemView) {
            super(itemView, SINGLE);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleLastRecord);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleTime);
            mHeadPortraitImageView = (HeadPortraitImageView) itemView.findViewById(R.id.ConversationAdapter_mIvSingleHeadImage);
        }
    }

    private final static class GroupView extends ItemView {
        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;
        HeadPortraitImageView mHeadPortraitImageView;

        GroupView(View itemView) {
            super(itemView, GROUP);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastRecord);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupTime);
            mHeadPortraitImageView = (HeadPortraitImageView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastHeadImage);
        }
    }
}
