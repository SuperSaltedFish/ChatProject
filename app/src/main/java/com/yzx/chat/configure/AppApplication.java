package com.yzx.chat.configure;

import android.app.ActivityManager;
import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.yzx.chat.tool.AuthenticationManager;

import java.util.List;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AppApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        AuthenticationManager.init(this,
                Constants.PREFERENCES_AUTHENTICATION,
                Constants.RSA_KEY_ALIAS,
                Constants.AES_KEY_ALIAS,
                Constants.TOKEN_ALIAS,
                Constants.DEVICE_ID_ALIAS);

        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        if (processAppName == null || !processAppName.equalsIgnoreCase(getPackageName())) {

        } else {
         //   initChat();
        }
    }

    private void initChat() {

        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        //初始化
        EMClient.getInstance().init(this, options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
    }

    private String getAppName(int pID) {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        for (Object aL : l) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (aL);
            try {
                if (info.pid == pID) {
                    return info.processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return null;
    }
}
