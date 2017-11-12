package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.GlideUtil;

import java.util.List;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageViewHolder> {
    private List<EMMessage> mMessageList;

    private static final int MESSAGE_SEND = 1;
    private static final int MESSAGE_RECEIVE = 2;

    public ChatMessageAdapter(List<EMMessage> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_RECEIVE) {
            return new ReceiveViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_receive, parent, false));
        } else {
            return new SendViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_send, parent, false));
        }
    }

    @Override
    public void bindDataToViewHolder(MessageViewHolder holder, int position) {
        EMMessage message = mMessageList.get(getItemCount()-position-1);
        holder.reset();

        switch (message.getType()) {
            case TXT:
                holder.mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
                holder.mTvTextContent.setVisibility(View.VISIBLE);
                break;
        }

        switch (mMessageList.get(getItemCount()-position-1).direct()) {
            case SEND:
                break;
            case RECEIVE:
                ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
                GlideUtil.loadFromUrl(mContext, receiveViewHolder.mIvAvatar, R.drawable.temp_head_image);
                break;
        }

    }


    @Override
    public int getItemCount() {
        if (mMessageList == null)
            return 0;
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(getItemCount()-position-1).direct() == EMMessage.Direct.RECEIVE) {
            return MESSAGE_RECEIVE;
        } else {
            return MESSAGE_SEND;
        }
    }

    static abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mTvTextContent;
        ImageView mIvImageContent;

        MessageViewHolder(View itemView) {
            super(itemView);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
            mIvImageContent = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }

        void reset() {
            GlideUtil.clear(itemView.getContext(), mIvImageContent);
            mTvTextContent.setVisibility(View.GONE);
            mIvImageContent.setVisibility(View.GONE);
            mTvTextContent.setText(null);
            mIvImageContent.setImageBitmap(null);
        }
    }

    private static class SendViewHolder extends MessageViewHolder {

        SendViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ReceiveViewHolder extends MessageViewHolder {
        ImageView mIvAvatar;

        ReceiveViewHolder(View itemView) {
            super(itemView);
            mIvAvatar = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
        }
    }
}
