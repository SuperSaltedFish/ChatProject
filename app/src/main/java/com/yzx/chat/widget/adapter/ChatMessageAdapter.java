package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;

import java.util.List;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int TYPE_LOAD_MORE = 0;
    private static final int TYPE_SEND_MESSAGE_TEXT = 1;
    private static final int TYPE_RECEIVE_MESSAGE_TEXT = 2;

    private List<EMMessage> mMessageList;
    private OnScrollToBottomListener mScrollToBottomListener;
    private String mLoadMoreHint;

    private static boolean isEnableLoadMore;

    public ChatMessageAdapter(List<EMMessage> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageViewHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_RECEIVE_MESSAGE_TEXT:
                return new ReceiveViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case TYPE_SEND_MESSAGE_TEXT:
                return new SendViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false));
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_load_more, parent, false));
            default:
                return null;
        }

    }

    @Override
    public void bindDataToViewHolder(MessageViewHolder holder, int position) {
        if (isEnableLoadMore && getItemCount() - 1 == position) {
            if (mScrollToBottomListener != null) {
                mScrollToBottomListener.OnScrollToBottom();
            }
            LoadMoreViewHolder loadMoreHolder = (LoadMoreViewHolder) holder;
            loadMoreHolder.mTvHintContent.setText(mLoadMoreHint);
            return;
        }
        EMMessage message = mMessageList.get(getPosition(position));
        switch (getItemViewType(position)) {
            case TYPE_SEND_MESSAGE_TEXT:
                break;
            case TYPE_RECEIVE_MESSAGE_TEXT:
                break;
        }

        switch (message.direct()) {
            case SEND:
                SendViewHolder sendViewHolder = (SendViewHolder) holder;
                sendViewHolder.mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
                break;
            case RECEIVE:
                ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
                receiveViewHolder.mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
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
        EMMessage message = mMessageList.get(getPosition(position));
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            switch (message.getType()) {
                case TXT:
                    return TYPE_RECEIVE_MESSAGE_TEXT;
                default:
                    throw new RuntimeException("unknown type");
            }
        } else {
            switch (message.getType()) {
                case TXT:
                    return TYPE_SEND_MESSAGE_TEXT;
                default:
                    throw new RuntimeException("unknown type");
            }
        }
    }

    public void setScrollToBottomListener(OnScrollToBottomListener listener) {
        mScrollToBottomListener = listener;
    }

    public void setLoadMoreHint(String hint) {
        mLoadMoreHint = hint;
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

        SendViewHolder(View itemView) {
            super(itemView);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }
    }

    private static class ReceiveViewHolder extends MessageViewHolder {
        ImageView mIvAvatar;
        TextView mTvTextContent;

        ReceiveViewHolder(View itemView) {
            super(itemView);
            mIvAvatar = (ImageView) itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }
    }

    private static class LoadMoreViewHolder extends MessageViewHolder {
        TextView mTvHintContent;

        LoadMoreViewHolder(View itemView) {
            super(itemView);
            mTvHintContent = (TextView) itemView.findViewById(R.id.LoadMoreView_mTvHintContent);
        }
    }
}
