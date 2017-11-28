package com.yzx.chat.tool;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

import com.yzx.chat.R;
import com.yzx.chat.configure.AppApplication;

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

    private NotifyManager() {
        if (sNotifyManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mManager = (NotificationManager) AppApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mDefaultNotifyBuilder = new Notification.Builder(AppApplication.getAppContext())
                .setSmallIcon(R.drawable.ic_chat)
                .setColor(AndroidTool.getColor(R.color.theme_main_color));
    }

    public void notify(Bitmap icon, String title, String content) {
        mDefaultNotifyBuilder
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(content);
        mManager.notify(1, mDefaultNotifyBuilder.build());
        mDefaultNotifyBuilder.setLargeIcon((Bitmap) null);
    }

}
