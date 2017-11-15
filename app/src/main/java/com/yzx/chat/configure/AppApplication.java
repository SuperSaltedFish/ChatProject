package com.yzx.chat.configure;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.yzx.chat.broadcast.NetworkStateReceive;
import com.yzx.chat.tool.AndroidTool;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.LogUtil;

import java.util.List;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AppApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        String processAppName = getProcessName(this, android.os.Process.myPid());
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {
            initTool();
            initChat();
        }
    }


    private void initTool() {
        IdentityManager.init(this,
                Constants.PREFERENCES_AUTHENTICATION,
                Constants.RSA_KEY_ALIAS,
                Constants.AES_KEY_ALIAS,
                Constants.TOKEN_ALIAS,
                Constants.DEVICE_ID_ALIAS);

        NetworkStateReceive.init(this);

        AndroidTool.init(this);
    }

    private void initChat() {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        EMClient.getInstance().init(this, options);
        EMClient.getInstance().setDebugMode(false);
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
