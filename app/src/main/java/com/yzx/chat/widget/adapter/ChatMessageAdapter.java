package com.yzx.chat.widget.adapter;

import android.support.text.emoji.widget.EmojiTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;

import java.util.List;
import java.util.Locale;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int TYPE_LOAD_MORE = 0;
    private static final int TYPE_SEND_MESSAGE_TEXT = 1;
    private static final int TYPE_RECEIVE_MESSAGE_TEXT = 2;
    private static final int TYPE_SEND_MESSAGE_VOICE = 3;
    private static final int TYPE_RECEIVE_MESSAGE_VOICE = 4;

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
            case TYPE_SEND_MESSAGE_TEXT:
                return new SendTextMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false));
            case TYPE_RECEIVE_MESSAGE_TEXT:
                return new ReceiveTextMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case TYPE_SEND_MESSAGE_VOICE:
                return new SendVoiceMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false));
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
            holder.setDate(null);
            LoadMoreViewHolder loadMoreHolder = (LoadMoreViewHolder) holder;
            loadMoreHolder.mTvHintContent.setText(mLoadMoreHint);
        } else {
            holder.setDate(mMessageList.get(getPosition(position)));
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
                case VOICE:
                    return TYPE_RECEIVE_MESSAGE_VOICE;
                default:
                    throw new RuntimeException("unknown type");
            }
        } else {
            switch (message.getType()) {
                case TXT:
                    return TYPE_SEND_MESSAGE_TEXT;
                case VOICE:
                    return TYPE_SEND_MESSAGE_VOICE;
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

        public abstract void setDate(EMMessage message);

    }

    private static class SendTextMessageHolder extends MessageViewHolder {
        private TextView mTvTextContent;

        SendTextMessageHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(EMMessage message) {
            mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
        }
    }

    private static class ReceiveTextMessageHolder extends MessageViewHolder {
        private ImageView mIvAvatar;
        private EmojiTextView mTvTextContent;

        ReceiveTextMessageHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(EMMessage message) {
            mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
            GlideUtil.loadFromUrl(mTvTextContent.getContext(), mIvAvatar, R.drawable.temp_head_image);
        }
    }

    private static class SendVoiceMessageHolder extends MessageViewHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;

        SendVoiceMessageHolder(View itemView) {
            super(itemView);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
        }

        @Override
        public void setDate(EMMessage message) {
            EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
            int timeLength_ms = voiceBody.getLength()*1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d'", timeLength_ms/1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if(timeLength_ms>=ChatActivity.MAX_VOICE_RECORDER_DURATION/2){
                mFlPlayLayout.setMinimumWidth(2*rowWidth);
            }else {
                mFlPlayLayout.setMinimumWidth((int) (rowWidth*(1+timeLength_ms*2.0/ChatActivity.MAX_VOICE_RECORDER_DURATION)));
            }
        }
    }


    private static class LoadMoreViewHolder extends MessageViewHolder {
        TextView mTvHintContent;

        LoadMoreViewHolder(View itemView) {
            super(itemView);
            mTvHintContent = itemView.findViewById(R.id.LoadMoreView_mTvHintContent);
        }

        @Override
        public void setDate(EMMessage message) {

        }
    }
}
