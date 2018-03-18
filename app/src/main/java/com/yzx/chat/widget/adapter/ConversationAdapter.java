package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
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
        return new ConversationHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation_single, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = mConversationList.get(position);
        holder.mTvName.setText(conversation.getConversationTitle());
        holder.mTvLastRecord.setText(IMMessageUtil.getMessageDigest(conversation));
        holder.mTvTime.setText(DateUtil.msecToTime_HH_mm(conversation.getSentTime()));

        String avatarUri = conversation.getPortraitUrl();
        Object[] avatarList;
        if (TextUtils.isEmpty(avatarUri)) {
            avatarList = new Object[]{R.drawable.ic_avatar_default};
        } else {
            avatarList = avatarUri.split(",");
        }
        if(avatarList.length==0){
            avatarList = new Object[]{R.drawable.ic_avatar_default,R.drawable.ic_avatar_default};
        }
        holder.mBadgeImageView.setImageUrlList(avatarList);

        int unreadMsgCount = conversation.getUnreadMessageCount();
        if (unreadMsgCount > 0) {
            if (conversation.getNotificationStatus() == Conversation.ConversationNotificationStatus.NOTIFY) {
                holder.mBadgeImageView.setBadgeText(unreadMsgCount);
                holder.mBadgeImageView.setBadgeMode(BadgeImageView.MODE_SHOW);
            } else {
                holder.mBadgeImageView.setBadgeMode(BadgeImageView.MODE_SHOW_ONLY_SMALL_BACKGROUND);
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


    static class ConversationHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        BadgeImageView mBadgeImageView;
        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;

        ConversationHolder(View itemView) {
            super(itemView);
            mBadgeImageView = itemView.findViewById(R.id.ConversationAdapter_mIvAvatar);
            mTvName = itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = itemView.findViewById(R.id.ConversationAdapter_mTvLastMessage);
            mTvTime = itemView.findViewById(R.id.ConversationAdapter_mTvTime);
        }

    }
}
