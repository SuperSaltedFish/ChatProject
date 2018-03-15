package com.yzx.chat.widget.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.text.TextUtils;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.view.activity.ImageOriginalActivity;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageHolder> {

    public static final int HOLDER_TYPE_SEND_MESSAGE_TEXT = 1;
    public static final int HOLDER_TYPE_RECEIVE_MESSAGE_TEXT = 2;
    public static final int HOLDER_TYPE_SEND_MESSAGE_VOICE = 3;
    public static final int HOLDER_TYPE_RECEIVE_MESSAGE_VOICE = 4;
    public static final int HOLDER_TYPE_SEND_MESSAGE_IMAGE = 5;
    public static final int HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE = 6;
    public static final int HOLDER_TYPE_SEND_MESSAGE_LOCATION = 7;
    public static final int HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION = 8;

    private List<Message> mMessageList;

    private MessageCallback mMessageCallback;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                return new TextSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false));
            case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                return new TextReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false));
            case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                return new VoiceSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false));
            case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                return new VoiceReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_voice, parent, false));
            case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                return new ImageSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_image, parent, false));
            case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                return new ImageReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_image, parent, false));
            case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                return new LocationSendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_location, parent, false));
            case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                return new LocationReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_location, parent, false));
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
        return mMessageList == null ? 0 : mMessageList.size();
    }

    @Override
    public int getViewHolderType(int position) {
        Message message = mMessageList.get(position);

        switch (message.getObjectName()) {
            case "RC:TxtMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_TEXT : HOLDER_TYPE_RECEIVE_MESSAGE_TEXT;
            case "RC:VcMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_VOICE : HOLDER_TYPE_RECEIVE_MESSAGE_VOICE;
            case "RC:ImgMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_IMAGE : HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE;
            case "RC:LBSMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_LOCATION : HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION;
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
        if (holder instanceof ImageSendMessageHolder) {
            ImageSendMessageHolder imageSendMessageHolder = (ImageSendMessageHolder) holder;
            GlideUtil.clear(mContext, imageSendMessageHolder.mIvContent);
            imageSendMessageHolder.mIvContent.setImageBitmap(null);
        }
    }

    public void setMessageCallback(MessageCallback messageCallback) {
        mMessageCallback = messageCallback;
    }

    static class TextSendMessageHolder extends SendMessageHolder {
        private TextView mTvTextContent;

        TextSendMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_SEND_MESSAGE_TEXT);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    static class TextReceiveMessageHolder extends ReceiveMessageHolder {
        private EmojiTextView mTvTextContent;

        TextReceiveMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_RECEIVE_MESSAGE_TEXT);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    static class VoiceSendMessageHolder extends SendMessageHolder {
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
        protected void setDate(Message message) {
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

    static class VoiceReceiveMessageHolder extends ReceiveMessageHolder {
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
        protected void setDate(Message message) {
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

    public static class ImageSendMessageHolder extends SendMessageHolder {
        public ImageView mIvContent;

        ImageSendMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_SEND_MESSAGE_IMAGE);
            mIvContent = itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            Size size = BitmapUtil.getBitmapBoundsSize(imageMessage.getThumUri().getPath());
            int imageWidth = size.getWidth();
            int imageHeight = size.getHeight();

            if (imageWidth != 0 && imageHeight != 0) {
                float scale;
                if (imageWidth >= imageHeight) {
                    scale = mIvContent.getMaxWidth() / (float) imageWidth;
                } else {
                    scale = mIvContent.getMaxHeight() / (float) imageHeight;
                }
                imageWidth = (int) (imageWidth * scale);
                imageHeight = (int) (imageHeight * scale);
                ViewGroup.LayoutParams params = mIvContent.getLayoutParams();
                params.width = imageWidth;
                params.height = imageHeight;
            }
            GlideUtil.loadFromUrl(mIvContent.getContext(), mIvContent, imageMessage.getThumUri());
        }
    }

    static class ImageReceiveMessageHolder extends ReceiveMessageHolder {
        ImageView mIvContent;

        ImageReceiveMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE);
            mIvContent = itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            Size size = BitmapUtil.getBitmapBoundsSize(imageMessage.getThumUri().getPath());
            int imageWidth = size.getWidth();
            int imageHeight = size.getHeight();

            if (imageWidth != 0 && imageHeight != 0) {
                float scale;
                if (imageWidth >= imageHeight) {
                    scale = mIvContent.getMaxWidth() / (float) imageWidth;
                } else {
                    scale = mIvContent.getMaxHeight() / (float) imageHeight;
                }
                imageWidth = (int) (imageWidth * scale);
                imageHeight = (int) (imageHeight * scale);
                ViewGroup.LayoutParams params = mIvContent.getLayoutParams();
                params.width = imageWidth;
                params.height = imageHeight;
            }
            GlideUtil.loadFromUrl(mIvContent.getContext(), mIvContent, imageMessage.getThumUri());
        }
    }

    public static class LocationSendMessageHolder extends SendMessageHolder {
        ImageView mIvMapImage;
        TextView mTvTitle;
        TextView mTvAddress;


        LocationSendMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_SEND_MESSAGE_IMAGE);
            mIvMapImage = itemView.findViewById(R.id.ChatMessageAdapter_mIvMapImage);
            mTvTitle = itemView.findViewById(R.id.ChatMessageAdapter_mTvTitle);
            mTvAddress = itemView.findViewById(R.id.ChatMessageAdapter_mTvAddress);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            String poi = locationMessage.getPoi();
            String[] content = poi.split("/");
            if (content.length >= 1) {
                mTvTitle.setText(content[0]);
            }
            if (content.length > 1) {
                mTvAddress.setText(content[1]);
            }
            GlideUtil.loadFromUrl(itemView.getContext(), mIvMapImage, locationMessage.getImgUri());
        }
    }

    static class LocationReceiveMessageHolder extends ReceiveMessageHolder {
        ImageView mIvMapImage;
        TextView mTvTitle;
        TextView mTvAddress;


        LocationReceiveMessageHolder(View itemView) {
            super(itemView, HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE);
            mIvMapImage = itemView.findViewById(R.id.ChatMessageAdapter_mIvMapImage);
            mTvTitle = itemView.findViewById(R.id.ChatMessageAdapter_mTvTitle);
            mTvAddress = itemView.findViewById(R.id.ChatMessageAdapter_mTvAddress);
      
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            String poi = locationMessage.getPoi();
            String[] content = poi.split("/");
            if (content.length >= 1) {
                mTvTitle.setText(content[0]);
            }
            if (content.length > 1) {
                mTvAddress.setText(content[1]);
            }
            GlideUtil.loadFromUrl(itemView.getContext(), mIvMapImage, locationMessage.getImgUri());
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
        protected void setDate(Message message) {
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

    static abstract class MessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        Message mMessage;
        MessageCallback mMessageCallback;
        int mHolderType;

        MessageHolder(View itemView, int type) {
            super(itemView);
            mHolderType = type;
        }

        @CallSuper
        protected void setDate(Message message) {
            mMessage = message;
        }

        protected void setMessageCallback(MessageCallback messageCallback) {
            mMessageCallback = messageCallback;
        }

        protected void performClick() {
            switch (mHolderType) {
                case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                    performClickTextContent();
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:

                    break;
                case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                    performClickImageContent();
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                    performClickLocationContent();
                    break;
            }
        }

        private void performClickTextContent() {

        }

        private void performClickImageContent() {
            ImageMessage imageMessage = (ImageMessage) mMessage.getContent();
            String imagePath = imageMessage.getLocalUri().getPath();
            if (TextUtils.isEmpty(imagePath) || !new File(imagePath).exists()) {
                AndroidUtil.showToast(AndroidUtil.getString(R.string.ChatActivity_ImageAlreadyDeleted));
            } else {
                Activity stackTopActivity = AndroidUtil.getStackTopActivityInstance();
                if (stackTopActivity instanceof ChatActivity) {
                    Intent intent = new Intent(stackTopActivity, ImageOriginalActivity.class);
                    intent.putExtra(ImageOriginalActivity.INTENT_EXTRA_IMAGE_PATH, imagePath);
                    ChatMessageAdapter.ImageSendMessageHolder holder = (ChatMessageAdapter.ImageSendMessageHolder) this;
                    ViewCompat.setTransitionName(holder.mIvContent, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(, holder.mIvContent, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                    ActivityCompat.startActivity(stackTopActivity, intent, options.toBundle());
                }
            }
        }


        private void performClickLocationContent() {
            LocationMessage locationMessage = (LocationMessage) mMessage.getContent();
        }

    }


    public interface MessageCallback {
        void resendMessage(int position, Message message);

        void setVoiceMessageAsListened(Message message);
    }

}
