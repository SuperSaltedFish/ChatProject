package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.IMMessageUtil;
import com.yzx.chat.widget.view.BadgeImageView;

import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.push.RongPushClient;

import static com.yzx.chat.bean.ConversationBean.SINGLE;
import static com.yzx.chat.bean.ConversationBean.GROUP;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ConversationHolder> {


    private List<Conversation> mConversationList;

    public ConversationAdapter(List<Conversation> conversationList) {
        mConversationList = conversationList;
    }


    @Override
    public ConversationHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RongPushClient.ConversationType.PRIVATE.getValue()) {
            return new SingleHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_single, parent, false));
        } else {
            return new GroupHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_group, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = mConversationList.get(position);
        holder.mTvName.setText(conversation.getConversationTitle());
        holder.mTvLastRecord.setText(IMMessageUtil.getMessageDigest(conversation));
        holder.mTvTime.setText(DateUtil.msecToTime_HH_mm(conversation.getSentTime()));
        int unreadMsgCount = conversation.getUnreadMessageCount();
        if (unreadMsgCount > 0) {
            if (conversation.getNotificationStatus() == Conversation.ConversationNotificationStatus.NOTIFY) {
                holder.mBadgeImageView.setBadgeMode(BadgeImageView.MODE_SHOW_ONLY_SMALL_BACKGROUND);
            } else {
                holder.mBadgeImageView.setBadgeMode(BadgeImageView.MODE_SHOW);
                holder.mBadgeImageView.setBadgeText(unreadMsgCount);
            }
        } else {
            holder.mBadgeImageView.setBadgeMode(BadgeImageView.MODE_HIDE);
        }

    }

    @Override
    public int getViewHolderCount() {
        return mConversationList == null ? 0 : mConversationList.size();
    }

    @Override
    public int getViewHolderType(int position) {
        return mConversationList.get(position).getConversationType().getValue();
    }


    static abstract class ConversationHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        private int mConversationType;

        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;
        BadgeImageView mBadgeImageView;

        ConversationHolder(View itemView, int conversationType) {
            super(itemView);
            mConversationType = conversationType;

        }

        public int getConversationType() {
            return mConversationType;
        }

    }

    private final static class SingleHolder extends ConversationHolder {


        SingleHolder(View itemView) {
            super(itemView, SINGLE);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleLastMessage);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvSingleTime);
            mBadgeImageView = (BadgeImageView) itemView.findViewById(R.id.ConversationAdapter_mIvSingleAvatar);
        }
    }

    private final static class GroupHolder extends ConversationHolder {

        BadgeImageView mBadgeImageView;

        GroupHolder(View itemView) {
            super(itemView, GROUP);
            mTvName = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastMessage);
            mTvTime = (TextView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupTime);
            mBadgeImageView = (BadgeImageView) itemView.findViewById(R.id.ConversationAdapter_mTvGroupLastHeadImage);
        }
    }
}
