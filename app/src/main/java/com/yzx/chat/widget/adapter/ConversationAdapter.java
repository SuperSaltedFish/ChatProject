package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.widget.view.AvatarImageView;

import java.util.List;

import static com.yzx.chat.bean.ConversationBean.SINGLE;
import static com.yzx.chat.bean.ConversationBean.GROUP;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ConversationViewHolder> {


    private List<ConversationBean> mConversationList;

    public ConversationAdapter(List<ConversationBean> conversationList) {
        mConversationList = conversationList;
    }


    @Override
    public ConversationViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SINGLE) {
            return new SingleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_single, parent, false));
        } else {
            return new GroupViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_group, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ConversationViewHolder holder, int position) {
        ConversationBean conversation = mConversationList.get(position);
        holder.mTvName.setText(conversation.getName());
        holder.mTvLastRecord.setText(conversation.getLastMsgContent());
        holder.mTvTime.setText(DateUtil.msecToTime_HH_mm(conversation.getLastMsgTime()));
        int unreadMsgCount = conversation.getUnreadMsgCount();
        switch (conversation.getConversationType()) {
            case SINGLE:
                SingleViewHolder singleViewHolder = (SingleViewHolder) holder;
                if (unreadMsgCount > 0) {
                    singleViewHolder.mAvatarImageView.setDigitalMode(AvatarImageView.MODE_SHOW);
                    singleViewHolder.mAvatarImageView.setDigital(unreadMsgCount);
                } else {
                    singleViewHolder.mAvatarImageView.setDigitalMode(AvatarImageView.MODE_HIDE);
                }
                break;
            case GROUP:
                GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
                break;
        }
    }


    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onViewRecycled(ConversationViewHolder holder) {
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
        return mConversationList.get(position).getConversationType();
    }

    static abstract class ConversationViewHolder extends RecyclerView.ViewHolder {

        private int mConversationType;

        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;

        ConversationViewHolder(View itemView, int conversationType) {
            super(itemView);
            mConversationType = conversationType;

        }

        public int getConversationType() {
            return mConversationType;
        }

    }

    private final static class SingleViewHolder extends ConversationViewHolder {

        AvatarImageView mAvatarImageView;

        SingleViewHolder(View itemView) {
            super(itemView, SINGLE);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleLastMessage);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleTime);
            mAvatarImageView = (AvatarImageView) itemView.findViewById(R.id.ConversationAdapter_mIvSingleAvatar);
        }
    }

    private final static class GroupViewHolder extends ConversationViewHolder {

        AvatarImageView mAvatarImageView;

        GroupViewHolder(View itemView) {
            super(itemView, GROUP);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastMessage);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupTime);
            mAvatarImageView = (AvatarImageView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastHeadImage);
        }
    }
}
