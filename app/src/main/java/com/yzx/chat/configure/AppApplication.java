package com.yzx.chat.configure;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.util.AndroidHelper;

import java.util.List;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AppApplication extends Application {

    public static final int APP_STATE_UNINITIALIZED = 0;
    public static final int APP_STATE_INITIALIZING = 1;
    public static final int APP_STATE_NORMAL = 2;

    private static int sCurrentAppState;
    private static Context sApplicationContext;

    public static void setAppState(int state) {
        sCurrentAppState = state;
    }

    public static int getAppState() {
        return sCurrentAppState;
    }

    public static Context getAppContext(){
        return sApplicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(sApplicationContext ==null){
            sApplicationContext = this;
        }
        String processAppName = getProcessName(this, android.os.Process.myPid());
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {

            AndroidHelper.init(this);

            EmojiCompat.init(new BundledEmojiCompatConfig(this));

            AppClient.init(this);

            LeakCanary.install(this);
        }
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo info : runningApps) {
            if (info.pid == pid) {
                return info.processName;
            }
        }
        return null;
    }

}
