package com.yzx.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年10月23日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class NetworkStateReceive extends BroadcastReceiver{


    private static final int NETWORK_NONE = -1;

    private static final int NETWORK_MOBILE = 0;

    private static final int NETWORK_WIFI = 1;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    LogUtil.e("当前WiFi连接可用 ");
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    LogUtil.e("当前移动网络连接可用 ");
                }
            } else {
                LogUtil.e("当前没有网络连接，请确保你已经打开网络 ");
            }
        }
    }
}
