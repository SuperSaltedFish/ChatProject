package com.yzx.chat.widget.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import com.yzx.chat.util.LogUtil;
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

    private List<Message> mMessageList;
    private String mLoadMoreHint;
    private boolean isEnable;

    private MessageCallback mMessageCallback;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MessageHolder.HOLDER_TYPE_SEND_MESSAGE_TEXT:
                return new TextSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false));
            case MessageHolder.HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                return new TextReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case MessageHolder.HOLDER_TYPE_SEND_MESSAGE_VOICE:
                return new VoiceSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false));
            case MessageHolder.HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                return new VoiceReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_voice, parent, false));
            default:
                return null;
        }

    }

    @Override
    public void bindDataToViewHolder(MessageHolder holder, int position) {
        holder.setMessageCallback(mMessageCallback);
        holder.setDate(mMessageList.get(position));
    }

    @Override
    public int getViewHolderCount() {
        return mMessageList == null ? 0 : mMessageList.size() ;
    }

    @Override
    public int getViewHolderType(int position) {
        Message message = mMessageList.get(position);
        switch (message.getObjectName()) {
            case "RC:TxtMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? MessageHolder.HOLDER_TYPE_SEND_MESSAGE_TEXT : MessageHolder.HOLDER_TYPE_RECEIVE_MESSAGE_TEXT;
            case "RC:VcMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? MessageHolder.HOLDER_TYPE_SEND_MESSAGE_VOICE : MessageHolder.HOLDER_TYPE_RECEIVE_MESSAGE_VOICE;
            default:
                throw new NoSuchElementException("unknown type");
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof SendMessageHolder) {
            SendMessageHolder sendMessageHolder = (SendMessageHolder) holder;
            if (sendMessageHolder.mProgressDrawable != null) {
                sendMessageHolder.mProgressDrawable.stop();
            }
        }
    }

    public void setMessageCallback(MessageCallback messageCallback) {
        mMessageCallback = messageCallback;
    }

    private static class TextSendMessageHolder extends SendMessageHolder {
        private TextView mTvTextContent;

        TextSendMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_SEND_MESSAGE_TEXT);
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
            super(itemView, HOLDER_TYPE_RECEIVE_MESSAGE_TEXT);
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
        private ImageView mIvPlayIcon;
        private VoicePlayer mVoicePlayer;
        private AnimationDrawable mPlayAnimation;
        private Drawable mDefaultIcon;

        private Uri mVoiceUri;

        VoiceSendMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_SEND_MESSAGE_VOICE);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
            mIvPlayIcon = itemView.findViewById(R.id.ChatMessageAdapter_mIvPlayIcon);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
            mPlayAnimation = (AnimationDrawable) AndroidUtil.getDrawable(R.drawable.anim_play_voice_send);
            mDefaultIcon = AndroidUtil.getDrawable(R.drawable.ic_voice_sent_play3);
            mPlayAnimation.setTint(AndroidUtil.getColor(R.color.text_main_color_white));
            mDefaultIcon.setTint(AndroidUtil.getColor(R.color.text_main_color_white));
            mFlPlayLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVoiceUri == null) {
                        return;
                    }
                    String path = mVoiceUri.getPath();
                    if (TextUtils.isEmpty(path) || path.equals(mVoicePlayer.getCurrentPlayPath())) {
                        return;
                    }
                    mIvPlayIcon.setImageDrawable(mPlayAnimation);
                    mVoicePlayer.play(mVoiceUri.getPath(), new VoicePlayer.OnPlayStateChangeListener() {
                        @Override
                        public void onStartPlay() {
                            mPlayAnimation.start();
                            mMessageCallback.setVoiceMessageAsListened(mMessage);
                        }

                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer, boolean isStop) {
                            reset();
                        }

                        @Override
                        public void onError(String error) {
                            LogUtil.e(error);
                            reset();
                        }

                        private void reset() {
                            mPlayAnimation.stop();
                            mIvPlayIcon.setImageDrawable(mDefaultIcon);
                        }
                    });
                }
            });
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            int timeLength_ms = voiceMessage.getDuration() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d\"", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                rowWidth = 2 * rowWidth;
            } else {
                rowWidth = (int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION));
            }
            ViewGroup.LayoutParams layoutParams = mFlPlayLayout.getLayoutParams();
            layoutParams.width = rowWidth;
            mFlPlayLayout.setLayoutParams(layoutParams);
            mVoiceUri = voiceMessage.getUri();

            if (mVoicePlayer.isPlaying() && mVoicePlayer.getCurrentPlayPath().equals(mVoiceUri.getPath())) {
                mIvPlayIcon.setImageDrawable(mPlayAnimation);
                mPlayAnimation.start();
            } else {
                mPlayAnimation.stop();
                mIvPlayIcon.setImageDrawable(mDefaultIcon);
            }
        }
    }

    private static class VoiceReceiveMessageHolder extends ReceiveMessageHolder {
        private TextView mTvVoiceTimeLength;
        private FrameLayout mFlPlayLayout;
        private VoicePlayer mVoicePlayer;
        private ImageView mIvPlayIcon;
        private ImageView mIvListenedState;
        private AnimationDrawable mPlayAnimation;
        private Drawable mDefaultIcon;

        private Uri mVoiceUri;

        VoiceReceiveMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_RECEIVE_MESSAGE_VOICE);
            mTvVoiceTimeLength = itemView.findViewById(R.id.ChatAdapter_mTvVoiceTimeLength);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
            mFlPlayLayout = itemView.findViewById(R.id.ChatAdapter_mFlPlayLayout);
            mIvPlayIcon = itemView.findViewById(R.id.ChatMessageAdapter_mIvPlayIcon);
            mIvListenedState = itemView.findViewById(R.id.ChatAdapter_mIvListenedState);
            mPlayAnimation = (AnimationDrawable) AndroidUtil.getDrawable(R.drawable.anim_play_voice_receive);
            mDefaultIcon = AndroidUtil.getDrawable(R.drawable.ic_voice_receive_play3);
            mPlayAnimation.setTint(AndroidUtil.getColor(R.color.text_primary_color_black));
            mDefaultIcon.setTint(AndroidUtil.getColor(R.color.text_primary_color_black));
            mFlPlayLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVoiceUri == null) {
                        return;
                    }
                    String path = mVoiceUri.getPath();
                    if (TextUtils.isEmpty(path) || path.equals(mVoicePlayer.getCurrentPlayPath())) {
                        return;
                    }
                    mIvPlayIcon.setImageDrawable(mPlayAnimation);
                    mPlayAnimation.start();
                    mVoicePlayer.play(mVoiceUri.getPath(), new VoicePlayer.OnPlayStateChangeListener() {
                        @Override
                        public void onStartPlay() {
                            mPlayAnimation.start();
                            mMessageCallback.setVoiceMessageAsListened(mMessage);
                            setListenedState(true);
                        }

                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer, boolean isStop) {
                            reset();
                        }

                        @Override
                        public void onError(String error) {
                            LogUtil.e(error);
                            reset();
                        }

                        private void reset() {
                            mPlayAnimation.stop();
                            mIvPlayIcon.setImageDrawable(mDefaultIcon);
                        }
                    });
                }
            });
        }

        @Override
        public void setDate(Message message) {
            super.setDate(message);
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            int timeLength_ms = voiceMessage.getDuration() * 1000;
            mTvVoiceTimeLength.setText(String.format(Locale.getDefault(), "%d\"", timeLength_ms / 1000));
            int rowWidth = mFlPlayLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                rowWidth = 2 * rowWidth;
            } else {
                rowWidth = (int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION));
            }
            ViewGroup.LayoutParams layoutParams = mFlPlayLayout.getLayoutParams();
            layoutParams.width = rowWidth;
            mFlPlayLayout.setLayoutParams(layoutParams);
            mVoiceUri = voiceMessage.getUri();

            if (mVoicePlayer.isPlaying() && mVoicePlayer.getCurrentPlayPath().equals(mVoiceUri.getPath())) {
                mIvPlayIcon.setImageDrawable(mPlayAnimation);
                mPlayAnimation.start();
            } else {
                mPlayAnimation.stop();
                mIvPlayIcon.setImageDrawable(mDefaultIcon);
            }
            setListenedState(message.getReceivedStatus().isListened());
        }

        private void setListenedState(boolean isListened) {
            mIvListenedState.setVisibility(isListened ? View.INVISIBLE : View.VISIBLE);
        }

    }


    static abstract class MessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        Message mMessage;
        MessageCallback mMessageCallback;
        int mHolderType;

        static final int HOLDER_TYPE_SEND_MESSAGE_TEXT = 1;
        static final int HOLDER_TYPE_RECEIVE_MESSAGE_TEXT = 2;
        static final int HOLDER_TYPE_SEND_MESSAGE_VOICE = 3;
        static final int HOLDER_TYPE_RECEIVE_MESSAGE_VOICE = 4;

        MessageHolder(View itemView, int type) {
            super(itemView);
            mHolderType = type;
        }

        @CallSuper
        public void setDate(Message message) {
            mMessage = message;
        }

        public void setMessageCallback(MessageCallback messageCallback) {
            mMessageCallback = messageCallback;
        }
    }

    static abstract class ReceiveMessageHolder extends MessageHolder {
        ImageView mIvAvatar;

        ReceiveMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            GlideUtil.loadFromUrl(mIvAvatar.getContext(), mIvAvatar, R.drawable.temp_head_image);
        }

    }

    static abstract class SendMessageHolder extends MessageHolder {

        private ImageView mIvMessageState;
        private CircularProgressDrawable mProgressDrawable;

        SendMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvMessageState = itemView.findViewById(R.id.ChatMessageAdapter_mIvMessageState);
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
                    mProgressDrawable.setColorSchemeColors(AndroidUtil.getColor(R.color.colorAccent));
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
                                if (mMessageCallback != null) {
                                    mMessageCallback.resendMessage(getAdapterPosition(), mMessage);
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


    public interface MessageCallback {
        void resendMessage(int position, Message message);

        void setVoiceMessageAsListened(Message message);
    }

}
