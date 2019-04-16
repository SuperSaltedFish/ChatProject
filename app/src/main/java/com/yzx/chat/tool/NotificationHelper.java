package com.yzx.chat.tool;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yzx.chat.R;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.configure.GlideRequest;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.module.contact.view.NotificationMessageActivity;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.module.main.view.HomeActivity;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.view.GlideHexagonTransform;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.emoji.text.EmojiCompat;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


/**
 * Created by YZX on 2018年05月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NotificationHelper {

    private static final int LARGE_ICON_SIZE = (int) AndroidHelper.dip2px(56);

    private static final String CHANNEL_ID_CHAT_MESSAGE_TYPE = "1";
    private static final String CHANNEL_NAME_CHAT_MESSAGE_TYPE = "ChatMessage";

    private static final String CHANNEL_ID_CONTACT_OPERATION_TYPE = "2";
    private static final String CHANNEL_NAME_CONTACT_OPERATION_TYPE = "ContactOperation";

    private static final String ACTION_RECYCLE = "NotificationHelper.Recycle";
    private static final String ACTION_CHAT_MESSAGE = "NotificationHelper.ChatMessage";
    private static final String ACTION_CONTACT_OPERATION = "NotificationHelper.ContactOperation";

    @SuppressLint("StaticFieldLeak")
    private static NotificationHelper sNotificationHelper;


    public static NotificationHelper getInstance() {
        if (sNotificationHelper == null) {
            synchronized (NotificationHelper.class) {
                if (sNotificationHelper == null) {
                    sNotificationHelper = new NotificationHelper(AppApplication.getAppContext());
                }
            }
        }
        return sNotificationHelper;
    }

    @TargetApi(26)
    private static NotificationChannel getDefaultNotificationChannel(String id, String name) {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GREEN);
        channel.setVibrationPattern(new long[]{100, 200});
        channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
        return channel;
    }

    private static Notification.Builder getDefaultNotificationBuilder(Context context, String channelID) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelID);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        builder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification);

        return builder;
    }

    private Context mAppContext;
    private Notification.Builder mChatMessageTypeBuilder;
    private Notification.Builder mContactOperationTypeBuilder;
    private NotificationManager mNotificationMessage;
    private SparseArray<CustomTarget<Bitmap>> mSimpleTargetMap;
    private SparseArray<String> mNotificationTypeMap;

    private NotificationHelper(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mSimpleTargetMap = new SparseArray<>();
        mNotificationTypeMap = new SparseArray<>();
        mNotificationMessage = (NotificationManager) mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mChatMessageTypeBuilder = getDefaultNotificationBuilder(mAppContext, CHANNEL_ID_CHAT_MESSAGE_TYPE);
        mContactOperationTypeBuilder = getDefaultNotificationBuilder(mAppContext, CHANNEL_ID_CONTACT_OPERATION_TYPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationMessage.createNotificationChannel(getDefaultNotificationChannel(CHANNEL_ID_CHAT_MESSAGE_TYPE, CHANNEL_NAME_CHAT_MESSAGE_TYPE));
            mNotificationMessage.createNotificationChannel(getDefaultNotificationChannel(CHANNEL_ID_CONTACT_OPERATION_TYPE, CHANNEL_NAME_CONTACT_OPERATION_TYPE));
        }
        registerReceiver();
    }

    private void registerReceiver() {
        mAppContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = intent.getParcelableExtra(ACTION_CHAT_MESSAGE);
                if (message != null) {
                    String conversationID = message.getTargetId();
                    Conversation.ConversationType type = message.getConversationType();
                    HomeActivity homeActivity = ActivityHelper.getActivityInstance(HomeActivity.class);
                    if (!TextUtils.isEmpty(conversationID) && homeActivity != null) {
                        recycleNotification(conversationID.hashCode());
                        if (ActivityHelper.getStackTopActivityClass() != ChatActivity.class) {
                            ActivityHelper.finishActivitiesInStackAbove(HomeActivity.class);
                        }
                        switch (type) {
                            case PRIVATE:
                                ChatActivity.startActivity(homeActivity, conversationID, ChatActivity.CONVERSATION_TYPE_PRIVATE);
                                break;
                            case GROUP:
                                ChatActivity.startActivity(homeActivity, conversationID, ChatActivity.CONVERSATION_TYPE_GROUP);
                                break;
                        }
                    }
                }
            }
        }, new IntentFilter(ACTION_CHAT_MESSAGE));

        mAppContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ContactOperationEntity contactOperation = intent.getParcelableExtra(ACTION_CONTACT_OPERATION);
                if (contactOperation != null && TextUtils.isEmpty(contactOperation.getContactID())) {
                    recycleNotification(contactOperation.getContactID().hashCode());
                    Activity topActivity = ActivityHelper.getStackTopActivityInstance();
                    if (topActivity != null) {
                        NotificationMessageActivity.startActivity(topActivity);
                    }
                }
            }
        }, new IntentFilter(ACTION_CONTACT_OPERATION));

        mAppContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int id = intent.getIntExtra(ACTION_RECYCLE, -1);
                if (id != -1) {
                    recycleNotification(id);
                }
            }
        }, new IntentFilter(ACTION_RECYCLE));
    }


    public void showPrivateMessageNotification(Message message, ContactEntity contact, boolean isFullScreen) {
        String conversationID = contact.getUserProfile().getUserID();
        String title = contact.getName();
        String content = IMMessageHelper.getMessageDigest(message.getContent()).toString();
        String avatarUrl = contact.getUserProfile().getAvatar();
        int notificationID = conversationID.hashCode();
        long time = message.getSentTime();
        Intent contentIntent = new Intent(ACTION_CHAT_MESSAGE);
        contentIntent.putExtra(ACTION_CHAT_MESSAGE, message);

        showNotification(
                mChatMessageTypeBuilder,
                CHANNEL_ID_CHAT_MESSAGE_TYPE,
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent,
                isFullScreen);
    }

    public void showGroupMessageNotification(Message message, GroupEntity group, boolean isFullScreen) {
        String conversationID = group.getGroupID();
        String title = group.getName();
        String content = IMMessageHelper.getMessageDigest(message.getContent()).toString();
        String avatarUrl = null;
        int notificationID = conversationID.hashCode();
        long time = message.getSentTime();
        Intent contentIntent = new Intent(ACTION_CHAT_MESSAGE);
        contentIntent.putExtra(ACTION_CHAT_MESSAGE, message);

        showNotification(
                mChatMessageTypeBuilder,
                CHANNEL_ID_CHAT_MESSAGE_TYPE,
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent,
                isFullScreen);
    }

    public void showContactOperationNotification(ContactOperationEntity contactOperation, String content, boolean isFullScreen) {
        String id = contactOperation.getContactID();
        String title = contactOperation.getUserInfo().getNickname();
        String avatarUrl = contactOperation.getUserInfo().getAvatar();
        int notificationID = id.hashCode();
        long time = contactOperation.getTime();
        Intent contentIntent = new Intent(ACTION_CONTACT_OPERATION);
        contentIntent.putExtra(ACTION_CONTACT_OPERATION, contactOperation);

        showNotification(
                mContactOperationTypeBuilder,
                CHANNEL_ID_CONTACT_OPERATION_TYPE,
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent,
                isFullScreen);
    }


    private void showNotification(final Notification.Builder builder, final String channelID, final int notificationID, final String title, final String content, final long timestamp, final String largeIconUrl, final Intent contentIntent, final boolean isFullScreen) {
        final CharSequence compatStr = EmojiCompat.get().process(content);
        CustomTarget<Bitmap> customTarget = new CustomTarget<Bitmap>(LARGE_ICON_SIZE, LARGE_ICON_SIZE) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Intent recycleIntent = new Intent(ACTION_RECYCLE);
                recycleIntent.putExtra(ACTION_RECYCLE, notificationID);
                builder.setLargeIcon(resource)
                        .setContentTitle(title)
                        .setContentText(compatStr)
                        .setTicker(title + "：" + compatStr)
                        .setWhen(timestamp)
                        .setDeleteIntent(PendingIntent.getBroadcast(mAppContext, notificationID, recycleIntent, PendingIntent.FLAG_CANCEL_CURRENT));

                if (isFullScreen) {
                    builder.setFullScreenIntent(PendingIntent.getBroadcast(mAppContext, notificationID, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT), false);
                } else {
                    builder.setContentIntent(PendingIntent.getBroadcast(mAppContext, notificationID, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                }
                if (mNotificationTypeMap.indexOfKey(notificationID) >= 0) {
                    cancelNotification(notificationID);
                }
                mNotificationTypeMap.put(notificationID, channelID);
                mSimpleTargetMap.put(notificationID, this);
                mNotificationMessage.notify(notificationID, builder.build());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        };

        GlideRequest<Bitmap> glideRequest = GlideApp.with(mAppContext).asBitmap();
        if (!TextUtils.isEmpty(largeIconUrl)) {
            glideRequest.load(largeIconUrl)
                    .transforms(new GlideHexagonTransform())
                    .error(R.mipmap.ic_launcher)
                    .into(customTarget);
        } else {
            glideRequest.load(R.mipmap.ic_launcher)
                    .into(customTarget);
        }
    }

    public void cancelNotification(int notificationID) {
        mNotificationMessage.cancel(notificationID);
        recycleNotification(notificationID);
    }

    private void recycleNotification(int notificationID) {
        mNotificationTypeMap.delete(notificationID);
        CustomTarget<Bitmap> bitmapTarget = mSimpleTargetMap.get(notificationID);
        if (bitmapTarget != null) {
            GlideApp.with(mAppContext).clear(bitmapTarget);
            mSimpleTargetMap.delete(notificationID);
        }
    }

    public void cancelAllChatMessageNotification() {
        for (int i = 0, size = mNotificationTypeMap.size(); i < size; i++) {
            String channelID = mNotificationTypeMap.valueAt(i);
            if (CHANNEL_ID_CHAT_MESSAGE_TYPE.equals(channelID)) {
                cancelNotification(mNotificationTypeMap.keyAt(i));
            }
        }
    }


    public void cancelAllContactOperationNotification() {
        for (int i = 0, size = mNotificationTypeMap.size(); i < size; i++) {
            String channelID = mNotificationTypeMap.valueAt(i);
            if (CHANNEL_ID_CONTACT_OPERATION_TYPE.equals(channelID)) {
                cancelNotification(mNotificationTypeMap.keyAt(i));
            }
        }
    }


    public void cancelAllNotification() {
        for (int i = 0, size = mSimpleTargetMap.size(); i < size; i++) {
            int notificationID = mSimpleTargetMap.keyAt(i);
            cancelNotification(notificationID);
        }
        mSimpleTargetMap.clear();
        mNotificationTypeMap.clear();
    }


}

