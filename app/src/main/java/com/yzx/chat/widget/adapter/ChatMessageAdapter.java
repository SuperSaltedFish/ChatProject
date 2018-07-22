package com.yzx.chat.widget.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseLongArray;
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
import com.yzx.chat.bean.BasicInfoProvider;
import com.yzx.chat.mvp.view.activity.VideoPlayActivity;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.extra.ContactNotificationMessageEx;
import com.yzx.chat.network.chat.extra.VideoMessage;
import com.yzx.chat.tool.IMMessageHelper;
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
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ChatMessageAdapter extends BaseRecyclerViewAdapter<ChatMessageAdapter.MessageHolder> {

    private static final long TIME_HINT_INTERVAL = 3 * 60 * 1000;

    private static final int HOLDER_TYPE_SEND_MESSAGE_TEXT = 1;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_TEXT = 2;
    private static final int HOLDER_TYPE_SEND_MESSAGE_VOICE = 3;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_VOICE = 4;
    private static final int HOLDER_TYPE_SEND_MESSAGE_IMAGE = 5;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE = 6;
    private static final int HOLDER_TYPE_SEND_MESSAGE_LOCATION = 7;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION = 8;
    private static final int HOLDER_TYPE_SEND_MESSAGE_VIDEO = 9;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO = 10;
    private static final int HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE = 11;
    private static final int HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE = 12;

    private List<Message> mMessageList;
    private SparseLongArray mTimeSparseLongArray;
    private MessageCallback mMessageCallback;
    private BasicInfoProvider mBasicInfoProvider;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
        mTimeSparseLongArray = new SparseLongArray(64);
        registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public MessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_text, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_text, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_voice, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_voice, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_image, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_image, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_location, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_location, parent, false), viewType);
            case HOLDER_TYPE_SEND_MESSAGE_VIDEO:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_video, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_video, parent, false), viewType);
            case HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE:
            case HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE:
                return new NotificationMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_notification_message, parent, false), viewType);
            default:
                throw new NoSuchElementException("unknown type code:" + viewType);
        }
    }

    @Override
    public void bindDataToViewHolder(MessageHolder holder, int position) {
        holder.isEnableTimeHint = mTimeSparseLongArray.get(mMessageList.get(position).getMessageId(), -1) > 0;
        holder.setMessageCallback(mMessageCallback);
        if (holder instanceof ReceiveMessageHolder) {
            ((ReceiveMessageHolder) holder).setBasicInfoProvider(mBasicInfoProvider);
        }
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
            case "Custom:VideoMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_VIDEO : HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO;
            case "RC:GrpNtf":
                return HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE;
            case "RC:ContactNtf":
                return HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE;
            default:
                throw new NoSuchElementException("unknown type:" + message.getObjectName());
        }
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SendMessageHolder) {
            SendMessageHolder sendMessageHolder = (SendMessageHolder) holder;
            if (sendMessageHolder.mProgressDrawable != null) {
                sendMessageHolder.mProgressDrawable.stop();
            }
        }
        if (holder instanceof MessageHolder) {
            MessageHolder messageHolder = (MessageHolder) holder;
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
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mMessageList == null || mMessageList.size() < positionStart + itemCount) {
                return;
            }
            if (positionStart == 0) {
                long newestTime;
                if (mTimeSparseLongArray.size() > 0) {
                    newestTime = mTimeSparseLongArray.get(mTimeSparseLongArray.keyAt(mTimeSparseLongArray.size() - 1), 0);
                } else {
                    newestTime = 0;
                }
                Message message;
                for (int i = positionStart + itemCount - 1; i >= positionStart; i--) {
                    message = mMessageList.get(i);
                    long messageTime = message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime();
                    if (messageTime - newestTime >= TIME_HINT_INTERVAL) {
                        mTimeSparseLongArray.append(message.getMessageId(), messageTime);
                        newestTime = messageTime;
                    }
                }
            } else {
                long oldestTime;
                if (mTimeSparseLongArray.size() > 0) {
                    oldestTime = mTimeSparseLongArray.get(mTimeSparseLongArray.keyAt(0), 0);
                } else {
                    oldestTime = 0;
                }
                long temp = oldestTime;
                Message message;
                for (int i = positionStart, count = positionStart + itemCount; i < count; i++) {
                    message = mMessageList.get(i);
                    long messageTime = message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime();
                    if (oldestTime - messageTime >= TIME_HINT_INTERVAL) {
                        mTimeSparseLongArray.put(message.getMessageId(), messageTime);
                        oldestTime = messageTime;
                    } else if (i == count - 1) {
                        if (oldestTime != temp) {
                            mTimeSparseLongArray.delete(mTimeSparseLongArray.keyAt(0));
                        }
                        mTimeSparseLongArray.put(message.getMessageId(), messageTime);
                    }
                }
            }

        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onChanged() {
            if (mMessageList == null || mMessageList.size() == 0) {
                mTimeSparseLongArray.clear();
            }
        }
    };

    public void setBasicInfoProvider(BasicInfoProvider basicInfoProvider) {
        mBasicInfoProvider = basicInfoProvider;
    }

    public void setMessageCallback(MessageCallback messageCallback) {
        mMessageCallback = messageCallback;
    }

    static final class NotificationMessageHolder extends MessageHolder {

        NotificationMessageHolder(View itemView, int type) {
            super(itemView, type);
        }

    }

    static final class ReceiveMessageHolder extends MessageHolder {
        ImageView mIvAvatar;
        TextView mTvNickname;
        BasicInfoProvider mBasicInfoProvider;

        ReceiveMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mTvNickname = itemView.findViewById(R.id.ChatMessageAdapter_mTvNickname);
        }

        void setBasicInfoProvider(BasicInfoProvider basicInfoProvider) {
            mBasicInfoProvider = basicInfoProvider;
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            if (mBasicInfoProvider != null) {
                GlideUtil.loadAvatarFromUrl(itemView.getContext(), mIvAvatar, mBasicInfoProvider.getAvatar(getAdapterPosition()));
                mTvNickname.setText(mBasicInfoProvider.getAvatar(getAdapterPosition()));
            }
        }
    }

    static final class SendMessageHolder extends MessageHolder {

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
        ItemViewHolder mViewHolder;
        boolean isEnableTimeHint;

        MessageHolder(View itemView, int type) {
            super(itemView);
            mHolderType = type;
            switch (mHolderType) {
                case HOLDER_TYPE_SEND_MESSAGE_TEXT:
                case HOLDER_TYPE_RECEIVE_MESSAGE_TEXT:
                    mViewHolder = new TextViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_VOICE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_VOICE:
                    mViewHolder = new VoiceViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_IMAGE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_IMAGE:
                    mViewHolder = new ImageViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_LOCATION:
                case HOLDER_TYPE_RECEIVE_MESSAGE_LOCATION:
                    mViewHolder = new LocationViewHolder(itemView);
                    break;
                case HOLDER_TYPE_SEND_MESSAGE_VIDEO:
                case HOLDER_TYPE_RECEIVE_MESSAGE_VIDEO:
                    mViewHolder = new VideoViewHolder(itemView);
                    break;
                case HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE:
                    mViewHolder = new GroupNotificationViewHolder(itemView);
                    break;
                case HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE:
                    mViewHolder = new ContactNotificationViewHolder(itemView);
                    break;
            }
            mViewHolder.mContentLayout.setOnClickListener(mOnContentClickListener);
        }


        @CallSuper
        protected void setDate(Message message) {
            mMessage = message;
            mViewHolder.parseMessageContent(mMessage);
            if (isEnableTimeHint) {
                mViewHolder.mTvTime.setText(IMMessageHelper.messageTimeToString(message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime()));
                mViewHolder.mTvTime.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.mTvTime.setVisibility(View.GONE);
            }
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
            voiceViewHolder.setEnableListenedState(true);
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
        TextView mTvTime;

        ItemViewHolder(View itemView) {
            mContentLayout = itemView.findViewById(R.id.ChatMessageAdapter_mContentLayout);
            mTvTime = itemView.findViewById(R.id.ChatMessageAdapter_mTvTime);
        }

        public abstract void parseMessageContent(Message message);
    }

    static final class ContactNotificationViewHolder extends ItemViewHolder {
        TextView mTvNotificationMessage;

        ContactNotificationViewHolder(View itemView) {
            super(itemView);
            mTvNotificationMessage = itemView.findViewById(R.id.ChatMessageAdapter_mTvNotificationMessage);
            mContentLayout = mTvNotificationMessage;
        }

        @Override
        public void parseMessageContent(Message message) {
            ContactNotificationMessage contactNotification = (ContactNotificationMessage) message.getContent();
            mTvNotificationMessage.setText(IMMessageHelper.contactNotificationMessageToString(contactNotification));
        }
    }

    static final class GroupNotificationViewHolder extends ItemViewHolder {
        TextView mTvNotificationMessage;

        GroupNotificationViewHolder(View itemView) {
            super(itemView);
            mTvNotificationMessage = itemView.findViewById(R.id.ChatMessageAdapter_mTvNotificationMessage);
            mContentLayout = mTvNotificationMessage;
        }

        @Override
        public void parseMessageContent(Message message) {
            GroupNotificationMessage groupNotification = (GroupNotificationMessage) message.getContent();
            mTvNotificationMessage.setText(IMMessageHelper.groupNotificationMessageToString(groupNotification));
        }
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
            mPlayAnimation = (AnimationDrawable) AndroidUtil.getDrawable(R.drawable.anim_play_voice);
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
