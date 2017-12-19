package com.yzx.chat.widget.adapter;

import android.media.MediaPlayer;
import android.support.annotation.CallSuper;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
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

import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;

import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.widget.listener.ResendMessageListener;

import java.util.List;
import java.util.Locale;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageHolder> {

    private static final int TYPE_LOAD_MORE = 0;
    private static final int TYPE_SEND_MESSAGE_TEXT = 1;
    private static final int TYPE_RECEIVE_MESSAGE_TEXT = 2;
    private static final int TYPE_SEND_MESSAGE_VOICE = 3;
    private static final int TYPE_RECEIVE_MESSAGE_VOICE = 4;

    private List<EMMessage> mMessageList;
    private String mLoadMoreHint;

    private ResendMessageListener mResendMessageListener;

    private boolean isEnableLoadMore;

    public ChatMessageAdapter(List<EMMessage> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SEND_MESSAGE_TEXT:
                return new TextSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false), mIvResendClickListener);
            case TYPE_RECEIVE_MESSAGE_TEXT:
                return new TextReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case TYPE_SEND_MESSAGE_VOICE:
                return new VoiceSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false), mIvResendClickListener);
            case TYPE_RECEIVE_MESSAGE_VOICE:
                return new VoiceReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_voice, parent, false));
            case TYPE_LOAD_MORE:
                return new LoadMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.view_load_more, parent, false));
            default:
                return null;
        }

    }

    @Override
    public void bindDataToViewHolder(MessageHolder holder, int position) {
        if (isEnableLoadMore && getItemCount() - 1 == position) {
            holder.setDate(null);
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.mTvHintContent.setText(mLoadMoreHint);
        } else {
            holder.setDate(mMessageList.get(getPosition(position)));
        }
    }


    @Override
    public void onViewDetachedFromWindow(MessageHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof SendMessageHolder) {
            SendMessageHolder sendMessageHolder = (SendMessageHolder) holder;
            if (sendMessageHolder.mProgressDrawable != null) {
                sendMessageHolder.mProgressDrawable.stop();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mMessageList == null)
            return 0;
        int size = mMessageList.size();
        if (size < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            isEnableLoadMore = false;
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

    private final View.OnClickListener mIvResendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mResendMessageListener != null) {
                mResendMessageListener.resendMessage((String) v.getTag());
            }
        }
    };

    public void setResendMessageListener(ResendMessageListener resendMessageListener) {
        mResendMessageListener = resendMessageListener;
    }

    public void setLoadMoreHint(String hint) {
        mLoadMoreHint = hint;
        if(isEnableLoadMore){
            notifyItemChanged(getItemCount()-1);
        }
    }

    private int getPosition(int position) {
        return mMessageList.size() - position - 1;
    }

    static abstract class MessageHolder extends RecyclerView.ViewHolder {
        protected String mMessageID;

        MessageHolder(View itemView) {
            super(itemView);
        }

        @CallSuper
        public void setDate(EMMessage message) {
            if (message != null) {
                mMessageID = message.getMsgId();
            }
        }

    }

    static abstract class ReceiveMessageHolder extends MessageHolder {
        private ImageView mIvAvatar;

        ReceiveMessageHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            GlideUtil.loadFromUrl(mIvAvatar.getContext(), mIvAvatar, R.drawable.temp_head_image);
        }
    }

    static abstract class SendMessageHolder extends MessageHolder {

        private ImageView mIvMessageState;
        private CircularProgressDrawable mProgressDrawable;
        private View.OnClickListener mResendClickListener;

        SendMessageHolder(View itemView, View.OnClickListener resendClickListener) {
            super(itemView);
            mResendClickListener = resendClickListener;
            mIvMessageState = itemView.findViewById(R.id.ChatMessageAdapter_mIvMessageState);
        }

        @Override
        public void setDate(EMMessage message) {
            super.setDate(message);
            setMessageState(message.status());
        }

        private void setMessageState(EMMessage.Status state) {
            mIvMessageState.setOnClickListener(null);
            mIvMessageState.setTag(mMessageID);
            if (state == EMMessage.Status.INPROGRESS) {
                if (mProgressDrawable == null) {
                    mProgressDrawable = new CircularProgressDrawable(itemView.getContext());
                    mProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
                    mProgressDrawable.setArrowEnabled(false);
                    mProgressDrawable.setStrokeWidth(AndroidUtil.dip2px(1));
                    mProgressDrawable.setColorSchemeColors(AndroidUtil.getColor(R.color.theme_main_color));
                    mIvMessageState.setImageDrawable(mProgressDrawable);
                }
                mProgressDrawable.start();
            } else {
                if (mProgressDrawable != null) {
                    mProgressDrawable.stop();
                    mProgressDrawable = null;
                }
                switch (state) {
                    case CREATE:
                    case FAIL:
                        mIvMessageState.setImageResource(R.drawable.ic_send_fail);
                        mIvMessageState.setOnClickListener(mResendClickListener);
                        break;
                    case SUCCESS:
                        mIvMessageState.setImageDrawable(null);
                        break;
                }
            }
        }

    }

    private static class TextSendMessageHolder extends SendMessageHolder {
        private TextView mTvTextContent;

        TextSendMessageHolder(View itemView, View.OnClickListener resendClickListener) {
            super(itemView, resendClickListener);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(EMMessage message) {
            super.setDate(message);
            mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
        }
    }

    private static class TextReceiveMessageHolder extends ReceiveMessageHolder {
        private EmojiTextView mTvTextContent;

        TextReceiveMessageHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(EMMessage message) {
            super.setDate(message);
            mTvTextContent.setText((((EMTextMessageBody) message.getBody()).getMessage()));
        }
    }

    private static class VoiceSendMessageHolder extends SendMessageHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;
        private VoicePlayer mVoicePlayer;

        VoiceSendMessageHolder(View itemView, View.OnClickListener resendClickListener) {
            super(itemView, resendClickListener);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
            mFlPlayLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVoicePlayer.play((String) v.getTag(), new VoicePlayer.OnPlayStateChangeListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {

                        }

                        @Override
                        public void onError() {

                        }
                    });
                }
            });
        }

        @Override
        public void setDate(EMMessage message) {
            super.setDate(message);
            EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
            int timeLength_ms = voiceBody.getLength() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d'", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                mFlPlayLayout.setMinimumWidth(2 * rowWidth);
            } else {
                mFlPlayLayout.setMinimumWidth((int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION)));
            }
            mFlPlayLayout.setTag(voiceBody.getLocalUrl());
        }
    }

    private static class VoiceReceiveMessageHolder extends ReceiveMessageHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;

        VoiceReceiveMessageHolder(View itemView) {
            super(itemView);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
        }

        @Override
        public void setDate(EMMessage message) {
            super.setDate(message);
            EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
            int timeLength_ms = voiceBody.getLength() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d'", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                mFlPlayLayout.setMinimumWidth(2 * rowWidth);
            } else {
                mFlPlayLayout.setMinimumWidth((int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION)));
            }
        }
    }


    private static class LoadMoreHolder extends MessageHolder {
        TextView mTvHintContent;

        LoadMoreHolder(View itemView) {
            super(itemView);
            mTvHintContent = itemView.findViewById(R.id.LoadMoreView_mTvHintContent);
        }
    }

}
