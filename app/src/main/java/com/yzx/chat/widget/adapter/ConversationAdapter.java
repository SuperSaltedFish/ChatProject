package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private Context mContext;
    private List<ConversationBean> mConversationList;

    public ConversationAdapter(Context context, List<ConversationBean> conversationList) {
        mConversationList = conversationList;
        mContext = context;
    }


    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SINGLE) {
            return new SingleView(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_single, parent, false));
        } else {
            return new GroupView(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_group, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        ConversationBean conversation = mConversationList.get(position);
        if (conversation.getConversationMode() == SINGLE) {
            SingleView singleView = (SingleView) holder;
            ConversationBean.Single single = (ConversationBean.Single) conversation;
            singleView.mTvName.setText(single.getName());
            singleView.mTvLastRecord.setText(single.getLastRecord());
            singleView.mTvTime.setText(single.getTime());
            singleView.mHeadPortraitImageView.setStateEnabled(true);
            singleView.itemView.setTag(single);
        } else {
            GroupView groupView = (GroupView) holder;
            ConversationBean.Group group = (ConversationBean.Group) conversation;
            groupView.mTvName.setText(group.getName());
            groupView.mTvLastRecord.setText(group.getLastRecord());
            groupView.mTvTime.setText(group.getTime());
            groupView.mHeadPortraitImageView.setStateEnabled(true);
            groupView.itemView.setTag(group);
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
