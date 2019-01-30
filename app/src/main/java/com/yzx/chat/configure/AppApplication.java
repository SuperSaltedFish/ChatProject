package com.yzx.chat.configure;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

import com.squareup.leakcanary.LeakCanary;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.net.framework.Executor.HttpExecutor;
import com.yzx.chat.util.AndroidUtil;

import java.util.List;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AppApplication extends Application {

    private static Context ApplicationContext;

    public static Context getAppContext(){
        return ApplicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(ApplicationContext==null){
            ApplicationContext = this;
        }
        String processAppName = getProcessName(this, android.os.Process.myPid());
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {

            AndroidUtil.init(this);

            EmojiCompat.init(new BundledEmojiCompatConfig(this));

            AppClient.init(this);

            HttpExecutor.init(this);
            HttpExecutor.getNetworkConfigure().setEnableNetworkStateCheck(true);

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
