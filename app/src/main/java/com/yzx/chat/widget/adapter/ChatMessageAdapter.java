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
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.GlideUtil;

import java.util.List;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageViewHolder> {
    private List<EMMessage> mMessageList;

    private static final int TYPE_LOAD_MORE = 0;
    private static final int TYPE_MESSAGE_SEND = 1;
    private static final int TYPE_MESSAGE_RECEIVE = 2;


    private static boolean isEnableLoadMore;

    public ChatMessageAdapter(List<EMMessage> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageViewHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_RECEIVE:
                return new ReceiveViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_receive, parent, false));
            case TYPE_MESSAGE_SEND:
                return new SendViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_send, parent, false));
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_load_more, parent, false));
            default:
                return null;
        }

    }

    @Override
    public void bindDataToViewHolder(MessageViewHolder holder, int position) {
        if (isEnableLoadMore) {
            if (position == getItemCount() - 1) {
                return;
            }
        }
        EMMessage message = mMessageList.get(getPosition(position));
        //  holder.reset();

        switch (message.getType()) {
            case TXT:
                break;
        }

        switch (mMessageList.get(getPosition(position)).direct()) {
            case SEND:
                SendViewHolder sendViewHolder = (SendViewHolder) holder;
                sendViewHolder.mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
                sendViewHolder.mTvTextContent.setVisibility(View.VISIBLE);
                break;
            case RECEIVE:
                ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
                receiveViewHolder.mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
                receiveViewHolder.mTvTextContent.setVisibility(View.VISIBLE);
                GlideUtil.loadFromUrl(mContext, receiveViewHolder.mIvAvatar, R.drawable.temp_head_image);
                break;
        }

    }


    @Override
    public int getItemCount() {
        if (mMessageList == null)
            return 0;
        int size = mMessageList.size();
        if (size < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            return size;
        } else {
            isEnableLoadMore = true;
            return size + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isEnableLoadMore && position == getItemCount() - 1) {
            return TYPE_LOAD_MORE;
        }
        if (mMessageList.get(getPosition(position)).direct() == EMMessage.Direct.RECEIVE) {
            return TYPE_MESSAGE_RECEIVE;
        } else {
            return TYPE_MESSAGE_SEND;
        }
    }

    private int getPosition(int position) {
        return mMessageList.size() - position - 1;
    }

    static abstract class MessageViewHolder extends RecyclerView.ViewHolder {

        MessageViewHolder(View itemView) {
            super(itemView);
        }

    }

    private static class SendViewHolder extends MessageViewHolder {
        TextView mTvTextContent;
        ImageView mIvImageContent;

        SendViewHolder(View itemView) {
            super(itemView);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
            mIvImageContent = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }
    }

    private static class ReceiveViewHolder extends MessageViewHolder {
        ImageView mIvAvatar;
        TextView mTvTextContent;
        ImageView mIvImageContent;

        ReceiveViewHolder(View itemView) {
            super(itemView);
            mIvAvatar = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
            mIvImageContent = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }
    }

    private static class LoadMoreViewHolder extends MessageViewHolder {

        public LoadMoreViewHolder(View itemView) {
            super(itemView);
        }
    }
}
