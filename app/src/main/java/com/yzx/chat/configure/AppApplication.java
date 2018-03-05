package com.yzx.chat.configure;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

import com.yzx.chat.tool.ActivityTool;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.network.chat.IMClient;

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

            DirectoryManager.init();

            ActivityTool.init(this);

            EmojiCompat.init(new BundledEmojiCompatConfig(this));

            IMClient.init(this,Constants.RONG_CLOUD_APP_KEY);
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
