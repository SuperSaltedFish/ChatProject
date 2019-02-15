package com.yzx.chat.widget.adapter;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.entity.BasicInfoProvider;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.common.view.ImageOriginalActivity;
import com.yzx.chat.module.common.view.LocationMapActivity;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.module.conversation.view.VideoPlayActivity;
import com.yzx.chat.tool.ActivityHelper;
import com.yzx.chat.tool.IMMessageHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.CountDownTimer;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.FileUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.RoundImageView;
import com.yzx.chat.widget.view.VisualizerView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.FileMessage;
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
    private static final int HOLDER_TYPE_SEND_MESSAGE_FILE = 11;
    private static final int HOLDER_TYPE_RECEIVE_MESSAGE_FILE = 12;
    private static final int HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE = 13;
    private static final int HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE = 14;

    private List<Message> mMessageList;
    private SparseLongArray mTimeDisplayPositionArray;
    private MessageCallback mMessageCallback;
    private BasicInfoProvider mBasicInfoProvider;
    private boolean isEnableNameDisplay;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
        mTimeDisplayPositionArray = new SparseLongArray(64);
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
            case HOLDER_TYPE_SEND_MESSAGE_FILE:
                return new SendMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_send_message_file, parent, false), viewType);
            case HOLDER_TYPE_RECEIVE_MESSAGE_FILE:
                return new ReceiveMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_receive_message_file, parent, false), viewType);
            case HOLDER_TYPE_GROUP_NOTIFICATION_MESSAGE:
            case HOLDER_TYPE_CONTACT_NOTIFICATION_MESSAGE:
                return new NotificationMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_notification_message, parent, false), viewType);
            default:
                throw new NoSuchElementException("unknown type code:" + viewType);
        }
    }

    @Override
    public void bindDataToViewHolder(MessageHolder holder, int position) {
        holder.isEnableTimeHint = mTimeDisplayPositionArray.get(mMessageList.get(position).getMessageId(), -1) > 0;
        holder.setMessageCallback(mMessageCallback);
        if (holder instanceof ReceiveMessageHolder) {
            ReceiveMessageHolder receiveMessageHolder = (ReceiveMessageHolder) holder;
            receiveMessageHolder.mBasicInfoProvider = mBasicInfoProvider;
            receiveMessageHolder.isEnableNameDisplay = isEnableNameDisplay;
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
            case "RC:FileMsg":
                return message.getMessageDirection() == Message.MessageDirection.SEND ? HOLDER_TYPE_SEND_MESSAGE_FILE : HOLDER_TYPE_RECEIVE_MESSAGE_FILE;
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
            messageHolder.mViewHolder.OnRecycle();
        }
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            calculateTimeDisplayPosition(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onChanged() {
            if (mMessageList == null || mMessageList.size() == 0) {
                mTimeDisplayPositionArray.clear();
            }
        }
    };

    private void calculateTimeDisplayPosition(int positionStart, int itemCount) {
        if (mMessageList == null || mMessageList.size() == 0) {
            mTimeDisplayPositionArray.clear();
            return;
        }
        if (mMessageList.size() <= positionStart) {
            return;
        }
        long latestTime;
        if (positionStart == 0 && mTimeDisplayPositionArray.size() > 0) {
            latestTime = mTimeDisplayPositionArray.get(mTimeDisplayPositionArray.keyAt(mTimeDisplayPositionArray.size() - 1), 0);
        } else {
            latestTime = 0;
        }
        Message message;
        for (int i = positionStart + itemCount - 1; i >= positionStart; i--) {
            message = mMessageList.get(i);
            long messageTime = message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime();
            if (Math.abs(latestTime - messageTime) >= TIME_HINT_INTERVAL) {
                mTimeDisplayPositionArray.append(message.getMessageId(), messageTime);
                latestTime = messageTime;
            }
        }
    }


    public void setEnableNameDisplay(boolean enableNameDisplay) {
        isEnableNameDisplay = enableNameDisplay;
    }

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
        boolean isEnableNameDisplay;

        ReceiveMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvAvatar = itemView.findViewById(R.id.ChatMessageAdapter_mIvAvatar);
            mTvNickname = itemView.findViewById(R.id.ChatMessageAdapter_mTvNickname);
        }

        @Override
        protected void setDate(Message message) {
            super.setDate(message);
            if (mBasicInfoProvider != null) {
                GlideUtil.loadAvatarFromUrl(itemView.getContext(), mIvAvatar, mBasicInfoProvider.getAvatar(message.getSenderUserId()));
                if (isEnableNameDisplay) {
                    mTvNickname.setText(mBasicInfoProvider.getName(message.getSenderUserId()));
                    mTvNickname.setVisibility(View.VISIBLE);
                } else {
                    mTvNickname.setVisibility(View.GONE);
                }
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
                    mProgressDrawable.setStrokeWidth(AndroidHelper.dip2px(1));
                    mProgressDrawable.setColorSchemeColors(AndroidHelper.getColor(R.color.colorAccent));
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
                        mIvMessageState.setOnClickListener(new OnOnlySingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {
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
                    if (type == HOLDER_TYPE_RECEIVE_MESSAGE_VOICE) {
                        ((VoiceViewHolder) mViewHolder).setVisualizerColor(AndroidHelper.getColor(R.color.colorAccentLight));
                    }
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
                case HOLDER_TYPE_SEND_MESSAGE_FILE:
                case HOLDER_TYPE_RECEIVE_MESSAGE_FILE:
                    mViewHolder = new FileViewHolder(itemView);
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

        private final View.OnClickListener mOnContentClickListener = new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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
                    AndroidHelper.showToast(AndroidHelper.getString(R.string.ChatActivity_ImageAlreadyOverdue));
                    return;
                }
            }
            Activity stackTopActivity = ActivityHelper.getStackTopActivityInstance();
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
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                voiceViewHolder.playVoice(path);
            }
            if (mMessage.getMessageDirection() == Message.MessageDirection.RECEIVE && !mMessage.getReceivedStatus().isListened()) {
                mMessageCallback.setVoiceMessageAsListened(mMessage);
                voiceViewHolder.setVoiceListened();
            }
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
                    AndroidHelper.showToast(AndroidHelper.getString(R.string.ChatActivity_VideoAlreadyOverdue));
                    return;
                }
            }
            Uri thumbUri = videoMessage.getThumbUri();
            Activity stackTopActivity = ActivityHelper.getStackTopActivityInstance();
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

        void OnRecycle() {
        }
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
        RoundImageView mIvImageContent;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIvImageContent = itemView.findViewById(R.id.ChatMessageAdapter_mIvImageContent);
            mContentLayout = mIvImageContent;
            mIvImageContent.setRoundRadius(AndroidHelper.dip2px(3));
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

        @Override
        void OnRecycle() {
            super.OnRecycle();
            GlideUtil.clear(mIvImageContent.getContext(), mIvImageContent);
            mIvImageContent.setImageBitmap(null);
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


        @Override
        void OnRecycle() {
            super.OnRecycle();
            GlideUtil.clear(mIvMapImage.getContext(), mIvMapImage);
            mIvMapImage.setImageBitmap(null);
        }
    }

    static final class VoiceViewHolder extends ItemViewHolder {
        TextView mTvVoiceDuration;
        VisualizerView mVisualizerView;
        ImageView mIvListenedState;
        ImageView mIvPlayIcon;
        private VoicePlayer mVoicePlayer;
        private String mVoicePath;
        private long mDurationMs;
        private CountDownTimer mCountDownTimer;

        VoiceViewHolder(View itemView) {
            super(itemView);
            mTvVoiceDuration = itemView.findViewById(R.id.ChatAdapter_mTvVoiceDuration);
            mVisualizerView = itemView.findViewById(R.id.ChatMessageAdapter_mVisualizerView);
            mIvListenedState = itemView.findViewById(R.id.ChatAdapter_mIvListenedState);
            mIvPlayIcon = itemView.findViewById(R.id.ChatMessageAdapter_mIvPlayIcon);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
        }

        @Override
        void OnRecycle() {
            super.OnRecycle();
            if (mVoicePlayer.isPlaying() && TextUtils.equals(mVoicePlayer.getCurrentPlayPath(), mVoicePath)) {
                mVoicePlayer.clearAllListener();
            }
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        }

        void setVisualizerColor(@ColorInt int color) {
            mVisualizerView.setStrokeColor(color);
        }

        void setVoiceListened() {
            if (mIvListenedState != null) {
                mIvListenedState.setVisibility(View.INVISIBLE);
            }
        }

        void playVoice(String path) {
            mVoicePath = path;
            if (mVoicePlayer.isPlaying() && TextUtils.equals(mVoicePlayer.getCurrentPlayPath(), mVoicePath)) {
                mVoicePlayer.stop();
            } else {
                mVoicePlayer.play(path, mOnPlayStateChangeListener, mOnDataCaptureListener);
            }
        }

        void reset() {
            mIvPlayIcon.setSelected(false);
            mVisualizerView.reset();
            setCurrentDuration(mDurationMs);
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
        }

        @Override
        public void parseMessageContent(Message message) {
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            mDurationMs = voiceMessage.getDuration();
            setCurrentDuration(mDurationMs);
            int rowWidth = mVisualizerView.getMinimumWidth();
            if (mDurationMs >= Constants.MAX_VOICE_RECORDER_DURATION / 2) {
                rowWidth = 2 * rowWidth;
            } else {
                rowWidth = (int) (rowWidth * (1 + mDurationMs * 2.0 / Constants.MAX_VOICE_RECORDER_DURATION));
            }
            ViewGroup.LayoutParams layoutParams = mVisualizerView.getLayoutParams();
            layoutParams.width = rowWidth;
            mVisualizerView.setLayoutParams(layoutParams);
            Uri voiceUri = voiceMessage.getUri();
            mVoicePath = voiceUri.getPath();
            if (mIvListenedState != null) {
                mIvListenedState.setVisibility(message.getReceivedStatus().isListened() ? View.INVISIBLE : View.VISIBLE);
            }
            if (mVoicePlayer.isPlaying() && TextUtils.equals(mVoicePlayer.getCurrentPlayPath(), mVoicePath)) {
                mVoicePlayer.setOnPlayStateChangeListenerIfPlaying(mOnPlayStateChangeListener);
                mVoicePlayer.setOnDataCaptureListenerIfPlaying(mOnDataCaptureListener);
                mIvPlayIcon.setSelected(true);
                startCountDownTimer(mDurationMs - mVoicePlayer.getAlreadyPlayTime());
            } else {
                reset();
            }
        }

        private void setCurrentDuration(long duration) {
            mTvVoiceDuration.setText(DateUtil.msecToDate_mm_ss(duration));
        }

        private void startCountDownTimer(long duration) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            mCountDownTimer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    setCurrentDuration(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    setCurrentDuration(mVoicePlayer.isPlaying() ? 0 : mDurationMs);
                }
            };
            mCountDownTimer.start();
        }

        private final VoicePlayer.OnPlayStateChangeListener mOnPlayStateChangeListener = new VoicePlayer.OnPlayStateChangeListener() {
            @Override
            public void onStartPlay() {
                mIvPlayIcon.setSelected(true);
                startCountDownTimer(mDurationMs);
            }

            @Override
            public void onCompletion(MediaPlayer mediaPlayer, boolean isStop) {
                reset();
            }

            @Override
            public void onError(String error) {
                LogUtil.e("play voice error: " + error);
                reset();
            }
        };

        private final Visualizer.OnDataCaptureListener mOnDataCaptureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                mVisualizerView.updateWaveform(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        };
    }

    static final class VideoViewHolder extends ItemViewHolder {
        RoundImageView mIvVideoThumbnail;
        TextView mTvVideoDuration;

        VideoViewHolder(View itemView) {
            super(itemView);
            mIvVideoThumbnail = itemView.findViewById(R.id.ChatMessageAdapter_mIvVideoThumbnail);
            mTvVideoDuration = itemView.findViewById(R.id.ChatMessageAdapter_mTvVideoDuration);
            mIvVideoThumbnail.setRoundRadius(AndroidHelper.dip2px(3));
            mContentLayout = mIvVideoThumbnail;
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

        @Override
        void OnRecycle() {
            super.OnRecycle();
            GlideUtil.clear(mIvVideoThumbnail.getContext(), mIvVideoThumbnail);
            mIvVideoThumbnail.setImageBitmap(null);
        }

        private static String videoTimeFormat(long millisecond) {
            int second = (int) (millisecond / 1000);
            return String.format(Locale.getDefault(), "%d:%02d", second / 60, second % 60);
        }
    }

    static final class FileViewHolder extends ItemViewHolder {
        TextView mTvFileName;
        TextView mTvFileSize;

        FileViewHolder(View itemView) {
            super(itemView);
            mTvFileName = itemView.findViewById(R.id.ChatMessageAdapter_mTvFileName);
            mTvFileSize = itemView.findViewById(R.id.ChatMessageAdapter_mTvFileSize);
        }

        @Override
        public void parseMessageContent(Message message) {
            FileMessage fileMessage = (FileMessage) message.getContent();
            LogUtil.e(fileMessage.toString());
            mTvFileName.setText(fileMessage.getName());
            mTvFileSize.setText(FileUtil.fileSizeFormat(fileMessage.getSize()));
        }

    }


    public interface MessageCallback {
        void resendMessage(int position, Message message);

        void setVoiceMessageAsListened(Message message);
    }

}
