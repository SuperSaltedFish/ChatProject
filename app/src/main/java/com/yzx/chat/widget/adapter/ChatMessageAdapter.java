package com.yzx.chat.widget.adapter;

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;

import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.view.activity.ChatActivity;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;


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

    private List<Message> mMessageList;
    private String mLoadMoreHint;
    private boolean isEnable;

    private OnResendItemClickListener mOnResendItemClickListener;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SEND_MESSAGE_TEXT:
                return new TextSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false));
            case TYPE_RECEIVE_MESSAGE_TEXT:
                return new TextReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case TYPE_SEND_MESSAGE_VOICE:
                return new VoiceSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false));
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
        if (position == getItemCount() - 1) {
            if (mLoadMoreHint == null || !isEnable) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
                loadMoreHolder.mTvHintContent.setText(mLoadMoreHint);
                holder.itemView.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder instanceof SendMessageHolder) {
                ((SendMessageHolder) holder).setOnResendItemClickListener(mOnResendItemClickListener);
            }
            holder.setDate(mMessageList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_LOAD_MORE;
        }
        Message message = mMessageList.get(position);
        switch (message.getObjectName()) {
            case "RC:TxtMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? TYPE_SEND_MESSAGE_TEXT : TYPE_RECEIVE_MESSAGE_TEXT;
            case "RC:VcMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? TYPE_SEND_MESSAGE_VOICE : TYPE_RECEIVE_MESSAGE_VOICE;
            default:
                throw new NoSuchElementException("unknown type");
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

    public void setLoadMoreHint(String hint) {
        mLoadMoreHint = hint;
  //      notifyItemChanged(getItemCount() - 1);
    }
    
    public void enableLoadMoreHint(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public void setOnResendItemClickListener(OnResendItemClickListener onResendItemClickListener) {
        mOnResendItemClickListener = onResendItemClickListener;
    }

    static abstract class MessageHolder extends RecyclerView.ViewHolder {
        Message mMessage;

        MessageHolder(View itemView) {
            super(itemView);
        }

        @CallSuper
        public void setDate(Message message) {
            mMessage = message;
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
        private OnResendItemClickListener mOnResendItemClickListener;

        SendMessageHolder(View itemView) {
            super(itemView);
            mIvMessageState = itemView.findViewById(R.id.ChatMessageAdapter_mIvMessageState);
        }

        public void setOnResendItemClickListener(OnResendItemClickListener onResendItemClickListener) {
            mOnResendItemClickListener = onResendItemClickListener;
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            setMessageState(message.getSentStatus());
        }

        private void setMessageState(Message.SentStatus state) {
            mIvMessageState.setOnClickListener(null);
            if (state == Message.SentStatus.SENDING) {
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
                    case FAILED:
                    case CANCELED:
                        mIvMessageState.setImageResource(R.drawable.ic_send_fail);
                        mIvMessageState.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnResendItemClickListener != null) {
                                    mOnResendItemClickListener.onResendItemClick(getAdapterPosition(),mMessage);
                                }
                            }
                        });
                        break;
                    default:
                        mIvMessageState.setImageDrawable(null);
                }
            }
        }

    }

    private static class TextSendMessageHolder extends SendMessageHolder {
        private TextView mTvTextContent;

        TextSendMessageHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    private static class TextReceiveMessageHolder extends ReceiveMessageHolder {
        private EmojiTextView mTvTextContent;

        TextReceiveMessageHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    private static class VoiceSendMessageHolder extends SendMessageHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;
        private VoicePlayer mVoicePlayer;

        private Uri mVoiceUri;

        VoiceSendMessageHolder(View itemView) {
            super(itemView);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
//            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
//            mFlPlayLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mVoicePlayer.play((String) v.getTag(), new VoicePlayer.OnPlayStateChangeListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mediaPlayer) {
//
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
//                }
//            });
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            int timeLength_ms = voiceMessage.getDuration() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d'", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                mFlPlayLayout.setMinimumWidth(2 * rowWidth);
            } else {
                mFlPlayLayout.setMinimumWidth((int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION)));
            }
            mVoiceUri = voiceMessage.getUri();
        }
    }

    private static class VoiceReceiveMessageHolder extends ReceiveMessageHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;

        private Uri mVoiceUri;

        VoiceReceiveMessageHolder(View itemView) {
            super(itemView);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            int timeLength_ms = voiceMessage.getDuration() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d'", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                mFlPlayLayout.setMinimumWidth(2 * rowWidth);
            } else {
                mFlPlayLayout.setMinimumWidth((int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION)));
            }
            mVoiceUri = voiceMessage.getUri();

        }
    }


    private static class LoadMoreHolder extends MessageHolder {
        TextView mTvHintContent;

        LoadMoreHolder(View itemView) {
            super(itemView);
            mTvHintContent = itemView.findViewById(R.id.LoadMoreView_mTvHintContent);
        }
    }

    public interface OnResendItemClickListener {
        void onResendItemClick(int position,Message message);
    }

}
