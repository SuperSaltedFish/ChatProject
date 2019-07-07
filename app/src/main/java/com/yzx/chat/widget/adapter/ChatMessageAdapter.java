package com.yzx.chat.widget.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.entity.BasicInfoProvider;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.common.view.ImageOriginalActivity;
import com.yzx.chat.tool.IMMessageHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.FileUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.widget.dialog.AlertDialog;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.RoundImageView;
import com.yzx.chat.widget.view.VisualizerView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
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
    private static final int HOLDER_TYPE_NOTIFICATION_MESSAGE = 13;

    private List<Message> mMessageList;
    private SparseLongArray mTimeDisplayStateArray;
    private MessageOperationCallback mMessageOperationCallback;
    private BasicInfoProvider mBasicInfoProvider;
    private boolean isEnableNameDisplay;

    public ChatMessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
        mTimeDisplayStateArray = new SparseLongArray(48);
        registerAdapterDataObserver();
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
            case HOLDER_TYPE_NOTIFICATION_MESSAGE:
                return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_notification_message, parent, false), viewType);
            default:
                throw new NoSuchElementException("unknown type code:" + viewType);
        }
    }

    @Override
    public void bindDataToViewHolder(MessageHolder holder, int position) {
        Message message = mMessageList.get(position);
        holder.setMessageOperationCallback(mMessageOperationCallback);
        holder.mViewHolder.setEnableTimeHint(mTimeDisplayStateArray.get(message.getMessageId(), -1) != -1);
        if (holder instanceof ReceiveMessageHolder) {
            ReceiveMessageHolder receiveMessageHolder = (ReceiveMessageHolder) holder;
            receiveMessageHolder.mBasicInfoProvider = mBasicInfoProvider;
            receiveMessageHolder.isEnableNameDisplay = isEnableNameDisplay;
        }
        holder.setData(message);
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
                return HOLDER_TYPE_NOTIFICATION_MESSAGE;
            case "RC:ContactNtf":
                return HOLDER_TYPE_NOTIFICATION_MESSAGE;
            default:
                throw new NoSuchElementException("unknown type:" + message.getObjectName());
        }
    }

    @Override
    public void onViewHolderRecycled(MessageHolder holder) {
        holder.onRecycle();
    }

    private void registerAdapterDataObserver() {
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mMessageList == null || mMessageList.size() <= positionStart) {
                    return;
                }
                int listSize = mMessageList.size();
                if (itemCount == 1) {
                    Message message = mMessageList.get(positionStart);
                    if (positionStart == listSize - 1) {
                        mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                    } else {
                        long latestTime = mTimeDisplayStateArray.get(mTimeDisplayStateArray.keyAt(mTimeDisplayStateArray.size() - 1), 0);
                        if (Math.abs(getMessageTime(message) - latestTime) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                            mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                        }
                    }
                } else {
                    long latestTime;
                    if (positionStart + itemCount == listSize) {//从最末尾插入
                        latestTime = 0;
                    } else { //从中间插入
                        latestTime = getMessageTime(mMessageList.get(positionStart + itemCount));
                    }
                    for (int i = positionStart + itemCount - 1; i >= positionStart; i--) {
                        Message message = mMessageList.get(i);
                        long messageTime = getMessageTime(message);
                        if (Math.abs(latestTime - messageTime) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                            mTimeDisplayStateArray.append(message.getMessageId(), messageTime);
                            latestTime = messageTime;
                        }
                    }
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                for (int i = toPosition, count = toPosition + itemCount; i < count; i++) {
                    mTimeDisplayStateArray.delete(mMessageList.get(i).getMessageId());
                }
                onItemRangeRemoved(fromPosition, itemCount);
                onItemRangeInserted(toPosition, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mMessageList == null || mMessageList.size() == 0) {
                    mTimeDisplayStateArray.clear();
                    return;
                }
                if (mMessageList.size() < positionStart || positionStart == 0) {
                    return;
                }
                int start = positionStart - 1;
                Message message = mMessageList.get(start);
                if (positionStart == mMessageList.size()) {
                    mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                } else {
                    Message nextItem = mMessageList.get(start + 1);
                    if (Math.abs(getMessageTime(message) - getMessageTime(nextItem)) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                        mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                    }
                }
            }
        });
    }

    private static long getMessageTime(Message message) {
        return message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime();
    }


    public void setEnableNameDisplay(boolean enableNameDisplay) {
        isEnableNameDisplay = enableNameDisplay;
    }

    public void setBasicInfoProvider(BasicInfoProvider basicInfoProvider) {
        mBasicInfoProvider = basicInfoProvider;
    }

    public void setMessageOperationCallback(MessageOperationCallback messageOperationCallback) {
        mMessageOperationCallback = messageOperationCallback;
    }


    static class MessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        Message mMessage;
        MessageOperationCallback mMessageOperationCallback;
        Holder mViewHolder;

        MessageHolder(View itemView, int type) {
            super(itemView);
            switch (type) {
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
                case HOLDER_TYPE_NOTIFICATION_MESSAGE:
                    mViewHolder = new NotificationViewHolder(itemView);
                    break;
            }
        }

        void onRecycle() {
            mViewHolder.onRecycle();
        }

        @CallSuper
        protected void setData(Message message) {
            mMessage = message;
            mViewHolder.parseMessageContent(mMessage);
        }

        void setMessageOperationCallback(MessageOperationCallback messageOperationCallback) {
            mMessageOperationCallback = messageOperationCallback;
        }

    }

    static final class ReceiveMessageHolder extends MessageHolder {
        ImageView mIvAvatar;
        TextView mTvNickname;
        BasicInfoProvider mBasicInfoProvider;
        boolean isEnableNameDisplay;

        ReceiveMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvAvatar = itemView.findViewById(R.id.mIvAvatar);
            mTvNickname = itemView.findViewById(R.id.mTvNickname);
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
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

        private ImageView mIvFailState;
        private ProgressBar mPbSendState;

        SendMessageHolder(View itemView, int type) {
            super(itemView, type);
            mIvFailState = itemView.findViewById(R.id.mIvFailState);
            mPbSendState = itemView.findViewById(R.id.mPbSendState);

            mIvFailState.setOnClickListener(new OnOnlySingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    new AlertDialog(v.getContext())
                            .setMessage(R.string.AlertDialogChat_Content)
                            .setTitle(R.string.AlertDialogChat_Title)
                            .setNegativeButton(R.string.AlertDialogChat_Cancel, null)
                            .setPositiveButton(R.string.AlertDialogChat_Resend, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mMessageOperationCallback.resendMessage(mMessage);
                                }
                            })
                            .show();
                }
            });
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
            Message.SentStatus status = message.getSentStatus();
            if (status == Message.SentStatus.SENDING) {
                mPbSendState.setVisibility(View.VISIBLE);
                mIvFailState.setVisibility(View.GONE);
            } else {
                mPbSendState.setVisibility(View.GONE);
                switch (status) {
                    case FAILED:
                    case CANCELED:
                        mIvFailState.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mIvFailState.setVisibility(View.GONE);
                }
            }
        }

    }

    abstract static class Holder {
        TextView mTvTime;
        Message mMessage;

        Holder(View itemView) {
            mTvTime = itemView.findViewById(R.id.mTvTime);
        }

        private void setEnableTimeHint(boolean isEnable) {
            mTvTime.setVisibility(isEnable ? View.VISIBLE : View.GONE);
        }

        @CallSuper
        public void parseMessageContent(Message message) {
            mMessage = message;
            if (mTvTime.getVisibility() == View.VISIBLE) {
                mTvTime.setText(IMMessageHelper.messageTimeToString(getMessageTime(mMessage)));
            }
        }

        void onRecycle() {
        }
    }

    static final class NotificationViewHolder extends Holder {
        TextView mTvNotificationMessage;

        NotificationViewHolder(View itemView) {
            super(itemView);
            mTvNotificationMessage = itemView.findViewById(R.id.mTvNotificationMessage);
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
            MessageContent content = message.getContent();
            if (content instanceof ContactNotificationMessage) {
                mTvNotificationMessage.setText(IMMessageHelper.contactNotificationMessageToString((ContactNotificationMessage) content));
            } else if (content instanceof GroupNotificationMessage) {
                mTvNotificationMessage.setText(IMMessageHelper.groupNotificationMessageToString((GroupNotificationMessage) content));
            }
        }
    }

    static final class TextViewHolder extends Holder {
        TextView mTvTextContent;

        TextViewHolder(View itemView) {
            super(itemView);
            mTvTextContent = itemView.findViewById(R.id.mTvTextContent);
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
            TextMessage textMessage = (TextMessage) message.getContent();
            mTvTextContent.setText(textMessage.getContent());
        }
    }

    static final class ImageViewHolder extends Holder {
        RoundImageView mIvImageContent;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIvImageContent = itemView.findViewById(R.id.mIvImageContent);
            mIvImageContent.setRoundRadius(AndroidHelper.dip2px(4));
            setupClickListener();
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
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

        void setupClickListener() {
            mIvImageContent.setOnClickListener(new OnOnlySingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    ImageMessage imageMessage = (ImageMessage) mMessage.getContent();
                    Uri imageUri = imageMessage.getLocalUri();
                    if (imageUri == null || TextUtils.isEmpty(imageUri.getPath()) || !new File(imageUri.getPath()).exists()) {
                        imageUri = imageMessage.getMediaUrl();
                        if (imageUri == null || TextUtils.isEmpty(imageUri.getPath())) {
                            AndroidHelper.showToast(AndroidHelper.getString(R.string.ChatActivity_ImageAlreadyOverdue));
                            return;
                        }
                    }
                    Activity activity = (Activity) v.getContext();
                    Intent intent = new Intent(activity, ImageOriginalActivity.class);
                    intent.putExtra(ImageOriginalActivity.INTENT_EXTRA_IMAGE_URI, imageUri);
                    intent.putExtra(ImageOriginalActivity.INTENT_EXTRA_THUMBNAIL_URI, imageMessage.getThumUri());
                    ViewCompat.setTransitionName(mIvImageContent, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, mIvImageContent, ImageOriginalActivity.TRANSITION_NAME_IMAGE);
                    ActivityCompat.startActivity(activity, intent, options.toBundle());
                }
            });
        }

        @Override
        void onRecycle() {
            super.onRecycle();
            GlideUtil.clear(mIvImageContent.getContext(), mIvImageContent);
            mIvImageContent.setImageBitmap(null);
        }
    }

    static final class LocationViewHolder extends Holder {
        ImageView mIvMapImage;
        TextView mTvTitle;
        TextView mTvAddress;

        LocationViewHolder(View itemView) {
            super(itemView);
            mIvMapImage = itemView.findViewById(R.id.mIvMapImage);
            mTvTitle = itemView.findViewById(R.id.mTvTitle);
            mTvAddress = itemView.findViewById(R.id.mTvAddress);
            setupClickListener();
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
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

        void setupClickListener() {
//            LocationMessage locationMessage = (LocationMessage) mMessage.getContent();
//            String poi = locationMessage.getPoi();
//            String[] content = poi.split("/");
//            PoiItem poiItem = new PoiItem(locationMessage.getExtra(), new LatLonPoint(locationMessage.getLat(), locationMessage.getLng()), content[0], content[1]);
//            LocationMapActivity.startOfShareType(mIvMapImage.getContext(), poiItem);
        }

        @Override
        void onRecycle() {
            super.onRecycle();
            GlideUtil.clear(mIvMapImage.getContext(), mIvMapImage);
            mIvMapImage.setImageBitmap(null);
        }
    }

    static final class VoiceViewHolder extends Holder {
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
            mVisualizerView = itemView.findViewById(R.id.mVisualizerView);
            mIvListenedState = itemView.findViewById(R.id.ChatAdapter_mIvListenedState);
            mIvPlayIcon = itemView.findViewById(R.id.mIvPlayIcon);
            mVoicePlayer = VoicePlayer.getInstance(itemView.getContext());
            setupClickListener();
        }

        @Override
        void onRecycle() {
            super.onRecycle();
            if (mVoicePlayer.isPlaying() && TextUtils.equals(mVoicePlayer.getCurrentPlayPath(), mVoicePath)) {
                mVoicePlayer.clearAllListener();
            }
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        }

        void setupClickListener() {
//            VoiceMessage voiceMessage = (VoiceMessage) mMessage.getContent();
//            Uri uri = voiceMessage.getUri();
//            final VoiceViewHolder voiceViewHolder = (VoiceViewHolder) mMessageHolder;
//            if (uri != null) {
//                String path = uri.getPath();
//                if (TextUtils.isEmpty(path)) {
//                    return;
//                }
//                voiceViewHolder.playVoice(path);
//            }
//            if (mMessage.getMessageDirection() == Message.MessageDirection.RECEIVE && !mMessage.getReceivedStatus().isListened()) {
//                mMessageOperationCallback.setVoiceMessageAsListened(mMessage);
//                voiceViewHolder.setVoiceListened();
//            }
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
            super.parseMessageContent(message);
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

    static final class VideoViewHolder extends Holder {
        RoundImageView mIvVideoThumbnail;
        TextView mTvVideoDuration;

        VideoViewHolder(View itemView) {
            super(itemView);
            mIvVideoThumbnail = itemView.findViewById(R.id.mIvVideoThumbnail);
            mTvVideoDuration = itemView.findViewById(R.id.mTvVideoDuration);
            mIvVideoThumbnail.setRoundRadius(AndroidHelper.dip2px(4));
            setupClickListener();
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
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

        void setupClickListener() {
//            VideoMessage videoMessage = (VideoMessage) mMessage.getContent();
//            Uri videoUri = videoMessage.getLocalPath();
//            String localPath = videoUri != null ? videoUri.getPath() : null;
//            if (TextUtils.isEmpty(localPath) || !new File(localPath).exists()) {
//                localPath = null;
//                videoUri = videoMessage.getMediaUrl();
//                if (videoUri == null || TextUtils.isEmpty(videoUri.getPath())) {
//                    AndroidHelper.showToast(AndroidHelper.getString(R.string.ChatActivity_VideoAlreadyOverdue));
//                    return;
//                }
//            }
//            Uri thumbUri = videoMessage.getThumbUri();
//            Activity stackTopActivity = ActivityHelper.getStackTopActivityInstance();
//            if (stackTopActivity instanceof ChatActivity) {
//                Intent intent = new Intent(stackTopActivity, VideoPlayActivity.class);
//                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_VIDEO_PATH, localPath);
//                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_THUMBNAIL_URI, thumbUri);
//                intent.putExtra(VideoPlayActivity.INTENT_EXTRA_MESSAGE, mMessage);
//                View transition = ((VideoViewHolder) mMessageHolder).mIvVideoThumbnail;
//                ViewCompat.setTransitionName(transition, VideoPlayActivity.TRANSITION_NAME_IMAGE);
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(stackTopActivity, transition, VideoPlayActivity.TRANSITION_NAME_IMAGE);
//                ActivityCompat.startActivityForResult(stackTopActivity, intent, 0, options.toBundle());
//            }
        }

        @Override
        void onRecycle() {
            super.onRecycle();
            GlideUtil.clear(mIvVideoThumbnail.getContext(), mIvVideoThumbnail);
            mIvVideoThumbnail.setImageBitmap(null);
        }

        private static String videoTimeFormat(long millisecond) {
            int second = (int) (millisecond / 1000);
            return String.format(Locale.getDefault(), "%d:%02d", second / 60, second % 60);
        }
    }

    static final class FileViewHolder extends Holder {
        TextView mTvFileName;
        TextView mTvFileSize;

        FileViewHolder(View itemView) {
            super(itemView);
            mTvFileName = itemView.findViewById(R.id.mTvFileName);
            mTvFileSize = itemView.findViewById(R.id.mTvFileSize);
        }

        @Override
        public void parseMessageContent(Message message) {
            super.parseMessageContent(message);
            FileMessage fileMessage = (FileMessage) message.getContent();
            LogUtil.e(fileMessage.toString());
            mTvFileName.setText(fileMessage.getName());
            mTvFileSize.setText(FileUtil.fileSizeFormat(fileMessage.getSize()));
        }
    }


    public interface MessageOperationCallback {
        void resendMessage(Message message);

        void setVoiceMessageAsListened(Message message);
    }

}
