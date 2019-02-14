package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.tool.IMMessageHelper;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.widget.view.BadgeView;
import com.yzx.chat.widget.view.NineGridAvatarView;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.emoji.text.EmojiCompat;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ConversationHolder> {


    private List<Conversation> mConversationList;
    private EmojiCompat mEmojiCompat;

    public ConversationAdapter(List<Conversation> conversationList) {
        mConversationList = conversationList;
        mEmojiCompat = EmojiCompat.get();
    }


    @Override
    public ConversationHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ConversationHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = mConversationList.get(position);
        holder.mTvName.setText(conversation.getConversationTitle());
        holder.mTvLastRecord.setText(mEmojiCompat.process(IMMessageHelper.getMessageDigest(conversation)));
        holder.mTvTime.setText(DateUtil.msecToTime_HH_mm(conversation.getSentTime()));

        String avatarUri = conversation.getPortraitUrl();
        Object[] avatarList;
        if (TextUtils.isEmpty(avatarUri)) {
            avatarList = new Object[]{R.drawable.ic_avatar_default};
        } else {
            avatarList = avatarUri.split(",");
        }

        holder.mIvAvatar.setImageUrlList(avatarList);

        holder.mTvBadge.setBadgeNumber(conversation.getUnreadMessageCount());
        if (conversation.getNotificationStatus() == Conversation.ConversationNotificationStatus.NOTIFY) {
            holder.mTvBadge.setBadgeBackgroundColor(ContextCompat.getColor(mContext, android.R.color.holo_red_light));
        } else {
            holder.mTvBadge.setBadgeBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccentLight));
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

        NineGridAvatarView mIvAvatar;
        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;
        BadgeView mTvBadge;

        ConversationHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.ConversationAdapter_mIvAvatar);
            mTvName = itemView.findViewById(R.id.ConversationAdapter_mTvName);
            mTvLastRecord = itemView.findViewById(R.id.ConversationAdapter_mTvLastMessage);
            mTvTime = itemView.findViewById(R.id.ConversationAdapter_mTvTime);
            mTvBadge = itemView.findViewById(R.id.ConversationAdapter_mTvBadge);
        }

    }
}
