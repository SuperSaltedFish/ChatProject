package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.widget.view.AvatarImageView;

import java.util.List;

import static com.yzx.chat.bean.ConversationBean.GROUP;
import static com.yzx.chat.bean.ConversationBean.SINGLE;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationAdapter extends BaseRecyclerViewAdapter<ConversationAdapter.ConversationViewHolder> {


    private List<EMConversation> mConversationList;

    public ConversationAdapter(List<EMConversation> conversationList) {
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
        EMConversation conversation = mConversationList.get(position);
        EMMessage message = conversation.getLastMessage();
        holder.mTvName.setText(conversation.conversationId());
        holder.mTvLastRecord.setText((((EMTextMessageBody) message.getBody()).getMessage()));
        holder.mTvTime.setText(DateUtil.msecToTime_HH_mm(message.getMsgTime()));
        int unreadMsgCount = conversation.getUnreadMsgCount();
        switch (conversation.getType()) {
            case Chat:
                SingleViewHolder singleViewHolder = (SingleViewHolder) holder;
                if(unreadMsgCount>0){
                    singleViewHolder.mAvatarImageView.setDigitalMode(AvatarImageView.MODE_SHOW);
                    singleViewHolder.mAvatarImageView.setDigital(unreadMsgCount);
                }else{
                    singleViewHolder.mAvatarImageView.setDigitalMode(AvatarImageView.MODE_HIDE);
                }
                break;
            case GroupChat:
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
        switch (mConversationList.get(position).getType()) {
            case Chat:
                return SINGLE;
            case GroupChat:
                return GROUP;
            default:
                return SINGLE;
        }
    }

    static abstract class ConversationViewHolder extends RecyclerView.ViewHolder {

        private int mConversationMode;

        TextView mTvName;
        TextView mTvLastRecord;
        TextView mTvTime;

        ConversationViewHolder(View itemView, int conversationMode) {
            super(itemView);
            mConversationMode = conversationMode;

        }

        public int getConversationMode() {
            return mConversationMode;
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
