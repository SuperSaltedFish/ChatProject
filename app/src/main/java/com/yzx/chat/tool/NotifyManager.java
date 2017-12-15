package com.yzx.chat.tool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;

import com.yzx.chat.R;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.AndroidUtil;

/**
 * Created by YZX on 2017年11月28日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class NotifyManager {

    private static NotifyManager sNotifyManager;

    public static NotifyManager getInstance() {
        if (sNotifyManager == null) {
            synchronized (NotifyManager.class) {
                if (sNotifyManager == null) {
                    sNotifyManager = new NotifyManager();
                }
            }
        }
        return sNotifyManager;
    }

    private NotificationManager mManager;
    private Notification.Builder mDefaultNotifyBuilder;
    private PendingIntent mLaunchPendingIntent;
    private final static int ID_NOTIFICATION = 10086;

    private NotifyManager() {
        if (sNotifyManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        Context context = AppApplication.getAppContext();

        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String packageName = context.getApplicationInfo().packageName;
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        mLaunchPendingIntent = PendingIntent.getActivity(context, ID_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mDefaultNotifyBuilder = new Notification.Builder(AppApplication.getAppContext())
                .setSmallIcon(R.drawable.ic_chat)
                .setColor(AndroidUtil.getColor(R.color.theme_main_color))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    public void notify(Bitmap icon, String title, String content) {
        if (icon != null) {
            mDefaultNotifyBuilder.setLargeIcon(icon);
        }
        mDefaultNotifyBuilder
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(title + ":" + content)
                .setAutoCancel(true);

        if (ActivityTool.isAppForeground()) {

        } else {
            mDefaultNotifyBuilder.setContentIntent(mLaunchPendingIntent);
        }
        mManager.notify(1, mDefaultNotifyBuilder.build());
        mDefaultNotifyBuilder.setLargeIcon((Bitmap) null);
    }

}
