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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ConversationHolder> {

    public ConversationAdapter() {
    }


    @Override
    public ConversationHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ConversationHolder(LayoutInflater.from(mContext).inflate(R.layout.item_conversation, parent, false));

    }

    @Override
    public void bindDataToViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = getItem(position);
        holder.mTvName.setText(conversation.getConversationTitle());
        holder.mTvLastRecord.setText(IMMessageHelper.getMessageDigest(conversation));
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
        return mAsyncListDiffer.getCurrentList().size();
    }

    @Override
    public int getViewHolderType(int position) {
        return getItem(position).getConversationType().getValue();
    }

    public Conversation getItem(int position) {
        return mAsyncListDiffer.getCurrentList().get(position);
    }

    public void submitList(List<Conversation> conversationList) {
        mAsyncListDiffer.submitList(conversationList);
    }

    private final AsyncListDiffer<Conversation> mAsyncListDiffer = new AsyncListDiffer<>(
            new BaseRecyclerViewAdapter.ListUpdateCallback(this),
            new AsyncDifferConfig.Builder<>(new DiffUtil.ItemCallback<Conversation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    return oldItem.getTargetId().equals(newItem.getTargetId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    if (oldItem.getLatestMessageId() != newItem.getLatestMessageId()) {
                        return false;
                    }
                    if (oldItem.getUnreadMessageCount() != newItem.getUnreadMessageCount()) {
                        return false;
                    }
                    if (oldItem.getSentTime() != newItem.getSentTime()) {
                        return false;
                    }
                    if (!oldItem.getConversationTitle().equals(newItem.getConversationTitle())) {
                        return false;
                    }
                    if (!oldItem.getNotificationStatus().equals(newItem.getNotificationStatus())) {
                        return false;
                    }
                    if (!TextUtils.equals(oldItem.getDraft(), newItem.getDraft())) {
                        return false;
                    }
                    return true;
                }

            }).build());


    static class ConversationHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        NineGridAvatarView mIvAvatar;
        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;
        BadgeView mTvBadge;

        ConversationHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.mIvAvatar);
            mTvName = itemView.findViewById(R.id.mTvName);
            mTvLastRecord = itemView.findViewById(R.id.mTvLastMessage);
            mTvTime = itemView.findViewById(R.id.mTvTime);
            mTvBadge = itemView.findViewById(R.id.mTvBadge);
        }

    }
}
