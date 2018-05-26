package com.yzx.chat.widget.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.CallSuper;
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

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.mvp.view.activity.VideoPlayActivity;
import com.yzx.chat.network.chat.extra.VideoMessage;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.mvp.view.activity.ChatActivity;
import com.yzx.chat.mvp.view.activity.ImageOriginalActivity;
import com.yzx.chat.mvp.view.activity.LocationMapActivity;

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
    public static final int HOLDER_TYPE_SEND_MESSAGE_VIDEO = 9;
    public static final int HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO = 10;

    private List<Message> mMessageList;

    private MessageCallback mMessageCallback;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                return new SendMessageHolder<TextViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                return new ReceiveMessageHolder<TextViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                return new SendMessageHolder<VoiceViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                return new ReceiveMessageHolder<VoiceViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_voice, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                return new SendMessageHolder<ImageViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_image, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                return new ReceiveMessageHolder<ImageViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_image, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                return new SendMessageHolder<LocationViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_location, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                return new ReceiveMessageHolder<LocationViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_location, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_VIDEO:
                return new SendMessageHolder<VideoViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_video, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO:
                return new ReceiveMessageHolder<VideoViewHolder>(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_video, parent, false), viewType);
            default:
                throw new NoSuchElementException("unknown type");
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
            case "Test:VideoMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_VIDEO : HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO;
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
        MessageHolder<?> messageHolder = (MessageHolder<?>) holder;
        switch (messageHolder.mHolderType) {
            case HOLDER_TYPE_SEND_MESSAGE_VOICE:
            case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                VoiceViewHolder voiceViewHolder = (VoiceViewHolder) messageHolder.mViewHolder;
                voiceViewHolder.setEnablePlayAnimation(false);
                break;
            case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
            case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                ImageViewHolder imageViewHolder = (ImageViewHolder) messageHolder.mViewHolder;
                GlideUtil.clear(mContext, imageViewHolder.mIvImageContent);
                imageViewHolder.mIvImageContent.setImageBitmap(null);
                break;
        }
    }

    public void setMessageCallback(MessageCallback messageCallback) {
        mMessageCallback = messageCallback;
    }


    static final class ReceiveMessageHolder<T extends ItemViewHolder> extends MessageHolder<T> {
        ImageView mIvAvatar;
        ViewGroup mContentLayout;

        ReceiveMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
            GlideUtil.loadFromUrl(mIvAvatar.getContext(), mIvAvatar, R.drawable.temp_head_image);
        }

    }

    static final class SendMessageHolder<T extends ItemViewHolder> extends MessageHolder<T> {

        private ImageView mIvMessageState;
        private CircularProgressDrawable mProgressDrawable;
        ViewGroup mContentLayout;

        SendMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvMessageState = itemView.findViewById(R.id.ChatMessageAdapter_mIvMessageState);
            mContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
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

    static abstract class MessageHolder<T extends ItemViewHolder> extends BaseRecyclerViewAdapter.BaseViewHolder {
        Message mMessage;
        ViewGroup mContentLayout;
        MessageCallback mMessageCallback;
        int mHolderType;
        T mViewHolder;

        MessageHolder(View itemView, int type) {
            super(itemView);
            mHolderType = type;
            mContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
            switch (mHolderType) {
                case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                    mViewHolder = (T) new TextViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                    mViewHolder = (T) new VoiceViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                    mViewHolder = (T) new ImageViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                    mViewHolder = (T) new LocationViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_VIDEO:
                case HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO:
                    mViewHolder = (T) new VideoViewHolder(itemView);
                    break;
                default:
                    throw new RuntimeException("Unknown holder type");
            }
            mViewHolder.mContentLayout.setOnClickListener(mOnContentClickListener);
        }

        @CallSuper
        protected void setDate(Message message) {
            mMessage = message;
            mViewHolder.parseMessageContent(mMessage);
        }

        void setMessageCallback(MessageCallback messageCallback) {
            mMessageCallback = messageCallback;
        }

        private final View.OnClickListener mOnContentClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mHolderType) {
                    case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                    case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                        performClickTextContent();
                        break;
                    case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                    case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                        performClickVoiceContent();
                        break;
                    case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                    case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                        performClickImageContent();
                        break;
                    case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                    case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                        performClickLocationContent();
                        break;
                    case HOLDER_TYPE_SEND_MESSAGE_VIDEO:
                    case HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO:
                        performClickVideoContent();
                        break;
                }
            }
        };

        void performClickTextContent() {

        }

        void performClickImageContent() {
            ImageMessage imageMessage = (ImageMessage) mMessage.getContent();
            Uri imageUri = imageMessage.getLocalUri();
            if (imageUri == null || TextUtils.isEmpty(imageUri.getPath()) || !new File(imageUri.getPath()).exists()) {
                imageUri = imageMessage.getMediaUrl();
                if (imageUri == null || TextUtils.isEmpty(imageUri.getPath())) {
                    AndroidUtil.showToast(AndroidUtil.getString(R.string.ChatActivity_ImageAlreadyOverdue));
                    return;
                }
            }
            Activity stackTopActivity = AndroidUtil.getStackTopActivityInstance();
            if (stackTopActivity instanceof ChatActivity) {
                Intent intent = new Intent(stackTopActivity, ImageOriginalActivity.class);
                intent.putExtra(ImageOriginalActivity.INTENT_EXTRA_IMAGE_URI, imageUri);
                intent.putExtra(ImageOriginalActivity.INTENT_EXTRA_THUMBNAIL_URI, imageMessage.getThumUri());
                View transition = ((ImageViewHolder) mViewHolder).mIvImageContent;
                ViewCompat.setTransitionName(transition, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(stackTopActivity, transition, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                ActivityCompat.startActivity(stackTopActivity, intent, options.toBundle());
            }
        }


        void performClickVoiceContent() {
            VoiceMessage voiceMessage = (VoiceMessage) mMessage.getContent();
            Uri uri = voiceMessage.getUri();
            final VoiceViewHolder voiceViewHolder = (VoiceViewHolder) mViewHolder;
            if (uri != null) {
                String path = uri.getPath();
                VoicePlayer voicePlayer = VoicePlayer.getInstance(itemView.getContext());
                if (TextUtils.isEmpty(path) || path.equals(voicePlayer.getCurrentPlayPath())) {
                    return;
                }
                voicePlayer.play(path, new VoicePlayer.OnPlayStateChangeListener() {
                    @Override
                    public void onStartPlay() {
                        voiceViewHolder.setEnablePlayAnimation(true);
                    }

                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer, boolean isStop) {
                        voiceViewHolder.setEnablePlayAnimation(false);
                    }

                    @Override
                    public void onError(String error) {
                        LogUtil.e("Play voice fail:" + error);
                        voiceViewHolder.setEnablePlayAnimation(false);
                    }
                });
            }
            voiceViewHolder.setEnableListenedState(false);
            mMessageCallback.setVoiceMessageAsListened(mMessage);
        }

        void performClickLocationContent() {
            LocationMessage locationMessage = (LocationMessage) mMessage.getContent();
            String poi = locationMessage.getPoi();
            String[] content = poi.split("/");
            PoiItem poiItem = new PoiItem(locationMessage.getExtra(), new LatLonPoint(locationMessage.getLat(), locationMessage.getLng()), content[0], content[1]);
            Intent intent = new Intent(itemView.getContext(), LocationMapActivity.class);
            intent.putExtra(LocationMapActivity.INTENT_EXTRA_POI, poiItem);
            itemView.getContext().startActivity(intent);
        }

        void performClickVideoContent() {
            VideoMessage videoMessage = (VideoMessage) mMessage.getContent();
            Uri videoUri = videoMessage.getLocalPath();
            String localPath = videoUri != null ? videoUri.getPath() : null;
            if (TextUtils.isEmpty(localPath) || !new File(localPath).exists()) {
                localPath = null;
                videoUri = videoMessage.getMediaUrl();
                if (videoUri == null || TextUtils.isEmpty(videoUri.getPath())) {
                    AndroidUtil.showToast(AndroidUtil.getString(R.string.ChatActivity_VideoAlreadyOverdue));
                    return;
                }
            }
            Uri thumbUri = videoMessage.getThumbUri();
            Activity stackTopActivity = AndroidUtil.getStackTopActivityInstance();
            if (stackTopActivity instanceof ChatActivity) {
                Intent intent = new Intent(stackTopActivity, VideoPlayActivity.class);
                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_VIDEO_PATH, localPath);
                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_THUMBNAIL_URI, thumbUri);
                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_MESSAGE, mMessage);
                View transition = ((VideoViewHolder) mViewHolder).mIvVideoThumbnail;
                ViewCompat.setTransitionName(transition, VideoPlayActivity.TRANSITION_NAME_IMAGE);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(stackTopActivity, transition, VideoPlayActivity.TRANSITION_NAME_IMAGE);
                ActivityCompat.startActivityForResult(stackTopActivity, intent, 0, options.toBundle());
            }
        }
    }

    abstract static class ItemViewHolder {
        View mContentLayout;

        ItemViewHolder(View itemView) {
            mContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
        }

        public abstract void parseMessageContent(Message message);
    }

    static final class TextViewHolder extends ItemViewHolder {
        TextView mTvTextContent;

        TextViewHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.ChatMessageAdapter_mTvTextContent);
            mContentLayout = mTvTextContent;
        }

        @Override
        public void parseMessageContent(Message message) {
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    static final class ImageViewHolder extends ItemViewHolder {
        ImageView mIvImageContent;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIvImageContent = itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
        }

        @Override
        public void parseMessageContent(Message message) {
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            Size size = BitmapUtil.getBitmapBoundsSize(imageMessage.getThumUri().getPath());
            int imageWidth = size.getWidth();
            int imageHeight = size.getHeight();

            if (imageWidth != 0 && imageHeight != 0) {
                float scale;
                if (imageWidth >= imageHeight) {
                    scale = mIvImageContent.getMaxWidth() / (float) imageWidth;
                } else {
                    scale = mIvImageContent.getMaxHeight() / (float) imageHeight;
                }
                imageWidth = (int) (imageWidth * scale);
                imageHeight = (int) (imageHeight * scale);
                ViewGroup.LayoutParams params = mIvImageContent.getLayoutParams();
                params.width = imageWidth;
                params.height = imageHeight;
            }
            GlideUtil.loadFromUrl(mIvImageContent.getContext(), mIvImageContent, imageMessage.getThumUri());
        }
    }

    static final class LocationViewHolder extends ItemViewHolder {
        ImageView mIvMapImage;
        TextView mTvTitle;
        TextView mTvAddress;

        LocationViewHolder(View itemView) {
            super(itemView);
            mIvMapImage = itemView.findViewById(R.id.ChatMessageAdapter_mIvMapImage);
            mTvTitle = itemView.findViewById(R.id.ChatMessageAdapter_mTvTitle);
            mTvAddress = itemView.findViewById(R.id.ChatMessageAdapter_mTvAddress);
        }

        @Override
        public void parseMessageContent(Message message) {
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            String poi = locationMessage.getPoi();
            String[] content = poi.split("/");
            if (content.length >= 1) {
                mTvTitle.setText(content[0]);
            }
            if (content.length > 1) {
                mTvAddress.setText(content[1]);
            }
            GlideUtil.loadFromUrl(mIvMapImage.getContext(), mIvMapImage, locationMessage.getImgUri());
        }
    }

    static final class VoiceViewHolder extends ItemViewHolder {
        TextView mTvVoiceDuration;
        ImageView mIvPlayIcon;
        ImageView mIvListenedState;
        private FrameLayout mFlContentLayout;
        private VoicePlayer mVoicePlayer;
        private AnimationDrawable mPlayAnimation;
        private Drawable mDefaultIcon;

        VoiceViewHolder(View itemView) {
            super(itemView);
            mTvVoiceDuration = itemView.findViewById(R.id.ChatAdapter_mTvVoiceDuration);
            mFlContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
            mIvPlayIcon = itemView.findViewById(R.id.ChatMessageAdapter_mIvPlayIcon);
            mIvListenedState = itemView.findViewById(R.id.ChatAdapter_mIvListenedState);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
            mPlayAnimation = (AnimationDrawable) AndroidUtil.getDrawable(R.drawable.anim_play_voice_send);
            mDefaultIcon = AndroidUtil.getDrawable(R.drawable.ic_voice_sent_play3);
            mPlayAnimation.setTint(AndroidUtil.getColor(R.color.textColorWhite));
            mDefaultIcon.setTint(AndroidUtil.getColor(R.color.textColorWhite));
        }

        public void setEnablePlayAnimation(boolean isEnable) {
            if (isEnable) {
                mIvPlayIcon.setImageDrawable(mPlayAnimation);
                mPlayAnimation.start();
            } else {
                mPlayAnimation.stop();
                mIvPlayIcon.setImageDrawable(mDefaultIcon);
            }
        }

        public void setEnableListenedState(boolean isEnable) {
            if (mIvListenedState != null) {
                mIvListenedState.setVisibility(isEnable ? View.INVISIBLE : View.VISIBLE);
            }
        }


        @Override
        public void parseMessageContent(Message message) {
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            int timeLength_ms = voiceMessage.getDuration() * 1000;
            mTvVoiceDuration.setText(String.format(Locale.getDefault(), "%d\"", timeLength_ms / 1000));
            int rowWidth = mFlContentLayout.getMinimumWidth();
            if (timeLength_ms >= ChatActivity.MAX_VOICE_RECORDER_DURATION / 2) {
                rowWidth = 2 * rowWidth;
            } else {
                rowWidth = (int) (rowWidth * (1 + timeLength_ms * 2.0 / ChatActivity.MAX_VOICE_RECORDER_DURATION));
            }
            ViewGroup.LayoutParams layoutParams = mFlContentLayout.getLayoutParams();
            layoutParams.width = rowWidth;
            mFlContentLayout.setLayoutParams(layoutParams);
            Uri voiceUri = voiceMessage.getUri();
            if (mVoicePlayer.isPlaying() && mVoicePlayer.getCurrentPlayPath().equals(voiceUri.getPath())) {
                mIvPlayIcon.setImageDrawable(mPlayAnimation);
                mPlayAnimation.start();
            } else {
                mPlayAnimation.stop();
                mIvPlayIcon.setImageDrawable(mDefaultIcon);
            }
            if (mIvListenedState != null) {
                setEnableListenedState(message.getReceivedStatus().isListened());
            }
        }
    }

    static final class VideoViewHolder extends ItemViewHolder {
        ImageView mIvVideoThumbnail;
        TextView mTvVideoDuration;

        VideoViewHolder(View itemView) {
            super(itemView);
            mIvVideoThumbnail = itemView.findViewById(R.id.ChatMessageAdapter_mIvVideoThumbnail);
            mTvVideoDuration = itemView.findViewById(R.id.ChatMessageAdapter_mTvVideoDuration);
        }

        @Override
        public void parseMessageContent(Message message) {
            VideoMessage videoMessage = (VideoMessage) message.getContent();
            Uri thumbnailUri = videoMessage.getThumbUri();
            if (thumbnailUri == null || TextUtils.isEmpty(thumbnailUri.getPath())) {
                return;
            }
            Size size = BitmapUtil.getBitmapBoundsSize(thumbnailUri.getPath());
            int thumbnailWidth = size.getWidth();
            int thumbnailHeight = size.getHeight();

            if (thumbnailWidth != 0 && thumbnailHeight != 0) {
                float scale;
                if (thumbnailWidth >= thumbnailHeight) {
                    scale = mIvVideoThumbnail.getMaxWidth() / (float) thumbnailWidth;
                } else {
                    scale = mIvVideoThumbnail.getMaxHeight() / (float) thumbnailHeight;
                }
                thumbnailWidth = (int) (thumbnailWidth * scale);
                thumbnailHeight = (int) (thumbnailHeight * scale);
                ViewGroup.LayoutParams params = mIvVideoThumbnail.getLayoutParams();
                params.width = Math.max(thumbnailWidth, mIvVideoThumbnail.getMinimumWidth());
                params.height = Math.max(thumbnailHeight, mIvVideoThumbnail.getMinimumHeight());
            }
            mTvVideoDuration.setText(videoTimeFormat(videoMessage.getDuration()));
            GlideUtil.loadFromUrl(mIvVideoThumbnail.getContext(), mIvVideoThumbnail, thumbnailUri);
        }

        private static String videoTimeFormat(long millisecond) {
            int second = (int) (millisecond / 1000);
            return String.format(Locale.getDefault(), "%d:%02d", second / 60, second % 60);
        }
    }


    public interface MessageCallback {
        void resendMessage(int position, Message message);

        void setVoiceMessageAsListened(Message message);
    }

}
