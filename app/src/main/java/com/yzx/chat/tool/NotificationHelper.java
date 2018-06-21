package com.yzx.chat.tool;

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

import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseArray;


import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yzx.chat.R;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.configure.GlideRequest;
import com.yzx.chat.mvp.view.activity.ChatActivity;
import com.yzx.chat.mvp.view.activity.HomeActivity;
import com.yzx.chat.mvp.view.activity.NotificationMessageActivity;
import com.yzx.chat.util.AndroidUtil;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


/**
 * Created by YZX on 2018年05月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NotificationHelper {

    private static final int LARGE_ICON_SIZE = (int) AndroidUtil.dip2px(56);

    private static final String CHANNEL_ID_CHAT_MESSAGE_TYPE = "1";
    private static final String CHANNEL_NAME_CHAT_MESSAGE_TYPE = "ChatMessage";

    private static final String CHANNEL_ID_CONTACT_OPERATION_TYPE = "2";
    private static final String CHANNEL_NAME_CONTACT_OPERATION_TYPE = "ContactOperation";

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
        return channel;
    }

    private static Notification.Builder getDefaultNotificationBuilder(Context context, String channelID) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelID);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        builder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent));
        return builder;
    }


    private Notification.Builder mChatMessageTypeBuilder;
    private Notification.Builder mContactOperationTypeBuilder;
    private NotificationManager mNotificationMessage;
    private Context mAppContext;
    private SparseArray<SimpleTarget<Bitmap>> mSimpleTargetMap;

    private NotificationHelper(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mSimpleTargetMap = new SparseArray<>();
        mNotificationMessage = (NotificationManager) mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mChatMessageTypeBuilder = getDefaultNotificationBuilder(mAppContext, CHANNEL_ID_CHAT_MESSAGE_TYPE);
        mContactOperationTypeBuilder = getDefaultNotificationBuilder(mAppContext, CHANNEL_ID_CONTACT_OPERATION_TYPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationMessage.createNotificationChannel(getDefaultNotificationChannel(CHANNEL_ID_CHAT_MESSAGE_TYPE, CHANNEL_NAME_CHAT_MESSAGE_TYPE));
            mNotificationMessage.createNotificationChannel(getDefaultNotificationChannel(CHANNEL_ID_CONTACT_OPERATION_TYPE, CHANNEL_NAME_CONTACT_OPERATION_TYPE));
        }
        appContext.registerReceiver(mRecycleNotificationBitmapReceiver, new IntentFilter(ACTION_RECYCLE));
        appContext.registerReceiver(mChatMessageReceiver, new IntentFilter(ACTION_CHAT_MESSAGE));
        appContext.registerReceiver(mContactOperationReceiver, new IntentFilter(ACTION_CONTACT_OPERATION));
    }

    public void showPrivateMessageNotification(Message message, ContactBean contact) {
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
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent);
    }

    public void showGroupMessageNotification(Message message, GroupBean group) {
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
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent);
    }

    public void showContactOperationNotification(ContactOperationBean contactOperation) {
        String id = contactOperation.getUserID();
        String title = contactOperation.getUser().getNickname();
        String content = contactOperation.getReason();
        if (TextUtils.isEmpty(content)) {
            content = AndroidUtil.getString(R.string.ContactOperationAdapter_DefaultReason);
        }
        String avatarUrl = contactOperation.getUser().getAvatar();
        int notificationID = id.hashCode();
        long time = contactOperation.getTime();
        Intent contentIntent = new Intent(ACTION_CONTACT_OPERATION);
        contentIntent.putExtra(ACTION_CONTACT_OPERATION, contactOperation);

        showNotification(
                mContactOperationTypeBuilder,
                notificationID,
                title,
                content,
                time,
                avatarUrl,
                contentIntent);
    }


    private void showNotification(final Notification.Builder builder, final int notificationID, final String title, final String content, final long timestamp, final String largeIconUrl, final Intent contentIntent) {
        final CharSequence compatStr = EmojiCompat.get().process(content);
        SimpleTarget<Bitmap> bitmapTarget = new SimpleTarget<Bitmap>(LARGE_ICON_SIZE, LARGE_ICON_SIZE) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Intent recycleIntent = new Intent(ACTION_RECYCLE);
                recycleIntent.putExtra(ACTION_RECYCLE, notificationID);
                builder.setLargeIcon(resource)
                        .setContentTitle(title)
                        .setContentText(compatStr)
                        .setTicker(title + "：" + compatStr)
                        .setWhen(timestamp)
                        .setDeleteIntent(PendingIntent.getBroadcast(mAppContext, notificationID, recycleIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                        .setContentIntent(PendingIntent.getBroadcast(mAppContext, notificationID, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                mNotificationMessage.notify(notificationID, builder.build());

                SimpleTarget<Bitmap> oldBitmapTarget = mSimpleTargetMap.get(notificationID);
                if (oldBitmapTarget != null) {
                    GlideApp.with(mAppContext).clear(oldBitmapTarget);
                }
                mSimpleTargetMap.put(notificationID, this);
            }
        };

        GlideRequest<Bitmap> glideRequest = GlideApp.with(mAppContext).asBitmap();
        if (!TextUtils.isEmpty(largeIconUrl)) {
            glideRequest.load(largeIconUrl)
                    .transforms(new CircleCrop())
                    .error(R.mipmap.ic_launcher)
                    .into(bitmapTarget);
        } else {
            glideRequest.load(R.mipmap.ic_launcher)
                    .transforms(new CircleCrop())
                    .into(bitmapTarget);
        }
    }

    public void cancelNotification(int notificationID) {
        mNotificationMessage.cancel(notificationID);
        SimpleTarget<Bitmap> bitmapTarget = mSimpleTargetMap.get(notificationID);
        if (bitmapTarget != null) {
            GlideApp.with(mAppContext).clear(bitmapTarget);
            mSimpleTargetMap.delete(notificationID);
        }
    }

    public void cancelAllNotification() {
        for (int i = 0, size = mSimpleTargetMap.size(); i < size; i++) {
            int notificationID = mSimpleTargetMap.keyAt(i);
            mNotificationMessage.cancel(notificationID);
            SimpleTarget<Bitmap> bitmapTarget = mSimpleTargetMap.get(notificationID);
            if (bitmapTarget != null) {
                GlideApp.with(mAppContext).clear(bitmapTarget);
            }
        }
        mSimpleTargetMap.clear();
    }

    private static final String ACTION_RECYCLE = "NotificationHelper.Recycle";
    private final BroadcastReceiver mRecycleNotificationBitmapReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra(ACTION_RECYCLE, -1);
            SimpleTarget<Bitmap> oldBitmapTarget = mSimpleTargetMap.get(id);
            if (oldBitmapTarget != null) {
                mSimpleTargetMap.delete(id);
                mNotificationMessage.cancel(id);
                GlideApp.with(mAppContext).clear(oldBitmapTarget);
            }
        }
    };

    private static final String ACTION_CHAT_MESSAGE = "NotificationHelper.ChatMessage";
    private final BroadcastReceiver mChatMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = intent.getParcelableExtra(ACTION_CHAT_MESSAGE);
            if (message != null) {
                String conversationID = message.getTargetId();
                Conversation.ConversationType type = message.getConversationType();
                HomeActivity homeActivity = AndroidUtil.getLaunchActivity(HomeActivity.class);
                if (!TextUtils.isEmpty(conversationID) && homeActivity != null) {
                    if (AndroidUtil.getStackTopActivityClass() != ChatActivity.class) {
                        AndroidUtil.finishActivitiesInStackAbove(HomeActivity.class);
                    }
                    Intent startActivityIntent = new Intent(homeActivity, ChatActivity.class);
                    startActivityIntent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, conversationID);
                    startActivityIntent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE_CODE, type.getValue());
                    homeActivity.startActivity(startActivityIntent);
                }
            }
        }
    };

    private static final String ACTION_CONTACT_OPERATION = "NotificationHelper.ContactOperation";
    private final BroadcastReceiver mContactOperationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContactOperationBean contactOperation = intent.getParcelableExtra(ACTION_CONTACT_OPERATION);
            if (contactOperation != null) {
                Activity topActivity = AndroidUtil.getStackTopActivityInstance();
                if (topActivity != null) {
                    topActivity.startActivity(new Intent(topActivity, NotificationMessageActivity.class));
                }
            }
        }
    };


}

// setFullScreenIntent(pendingIntent, false);
