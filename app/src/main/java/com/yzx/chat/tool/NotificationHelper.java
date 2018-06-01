package com.yzx.chat.tool;

import android.annotation.TargetApi;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.SparseArray;


import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yzx.chat.R;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.configure.GlideRequest;
import com.yzx.chat.util.AndroidUtil;

import io.rong.imlib.model.Message;


/**
 * Created by YZX on 2018年05月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NotificationHelper {
    private static final String CHANNEL_ID_CHAT_MESSAGE_TYPE = "1";
    private static final String CHANNEL_NAME_CHAT_MESSAGE_TYPE = "ChatMessage";

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
                .setSmallIcon(R.drawable.ic_conversation)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent));
        return builder;
    }


    private Notification.Builder mChatMessageTypeBuilder;
    private NotificationManager mNotificationMessage;
    private Context mAppContext;
    private SparseArray<SimpleTarget<Bitmap>> mSimpleTargetMap;

    private NotificationHelper(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mSimpleTargetMap = new SparseArray<>();
        mNotificationMessage = (NotificationManager) mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mChatMessageTypeBuilder = getDefaultNotificationBuilder(mAppContext, CHANNEL_ID_CHAT_MESSAGE_TYPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationMessage.createNotificationChannel(getDefaultNotificationChannel(CHANNEL_ID_CHAT_MESSAGE_TYPE, CHANNEL_NAME_CHAT_MESSAGE_TYPE));
        }
        appContext.registerReceiver(mRecycleNotificationBitmapReceiver, new IntentFilter(ACTION_RECYCLE));
        appContext.registerReceiver(mEnterDetailsReceiver, new IntentFilter(ACTION_ENTER_DETAILS));
    }

    public void showPrivateMessageNotification(Message message, ContactBean contact) {
        final String conversationID = contact.getUserProfile().getUserID();
        final String title = contact.getName();
        final String content = IMMessageHelper.getMessageDigest(message.getContent()).toString();
        final String ticker = title + "：" + content;
        final String avatarUrl = contact.getUserProfile().getAvatar();
        final int notificationID = conversationID.hashCode();
        final long time = message.getSentTime();
        int bitmapSize = (int) AndroidUtil.dip2px(56);

        SimpleTarget<Bitmap> bitmapTarget = new SimpleTarget<Bitmap>(bitmapSize,bitmapSize) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Intent recycleIntent = new Intent(ACTION_RECYCLE);
                recycleIntent.putExtra(ACTION_RECYCLE, notificationID);
                Intent enterDetailsIntent = new Intent(ACTION_ENTER_DETAILS);
                enterDetailsIntent.putExtra(ACTION_ENTER_DETAILS, conversationID);
                mChatMessageTypeBuilder
                        .setContentTitle(title)
                        .setContentText(content)
                        .setTicker(ticker)
                        .setWhen(time)
                        .setLargeIcon(resource)
                        .setDeleteIntent(PendingIntent.getBroadcast(mAppContext, notificationID, recycleIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                        .setContentIntent(PendingIntent.getBroadcast(mAppContext, notificationID, enterDetailsIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                mNotificationMessage.notify(notificationID, mChatMessageTypeBuilder.build());

                SimpleTarget<Bitmap> oldBitmapTarget = mSimpleTargetMap.get(notificationID);
                if (oldBitmapTarget != null) {
                    GlideApp.with(mAppContext).clear(oldBitmapTarget);
                }
                mSimpleTargetMap.put(notificationID, this);
            }
        };

        GlideRequest<Bitmap> glideRequest = GlideApp.with(mAppContext).asBitmap();
        if (!TextUtils.isEmpty(avatarUrl)) {
            glideRequest.load(avatarUrl)
                    .transforms(new CircleCrop())
                    .error(R.mipmap.ic_launcher)
                    .into(bitmapTarget);
        } else {
            glideRequest.load(R.mipmap.ic_launcher)
                    .transforms(new CircleCrop())
                    .into(bitmapTarget);
        }
    }

    private static final String ACTION_RECYCLE = "RecycleNotificationBitmapAction";
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

    private static final String ACTION_ENTER_DETAILS = "RecycleNotificationBitmapAction";
    private final BroadcastReceiver mEnterDetailsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


}

// setFullScreenIntent(pendingIntent, false);
