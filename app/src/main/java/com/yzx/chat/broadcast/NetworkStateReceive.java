package com.yzx.chat.broadcast;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yzx.chat.util.LogUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by YZX on 2017年10月23日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class NetworkStateReceive extends BroadcastReceiver {

    private static final String RECEIVE_INTENT_TYPE = ConnectivityManager.CONNECTIVITY_ACTION;

    private static int sCurrentNetworkType = -1;

    private static boolean sIsNetworkAvailable;

    private static List<NetworkChangeListener> sListenerList = new LinkedList<>();

    public static void init(Application context) {
        IntentFilter filter = new IntentFilter(RECEIVE_INTENT_TYPE);
        NetworkStateReceive receive = new NetworkStateReceive();
        context.registerReceiver(receive, filter);
    }

    public static synchronized void registerNetworkChangeListener(NetworkChangeListener listener) {
        if (!sListenerList.contains(listener)) {
            sListenerList.add(listener);
        }
    }

    public static synchronized void unregisterNetworkChangeListener(NetworkChangeListener listener) {
        sListenerList.remove(listener);
    }


    private NetworkStateReceive() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (RECEIVE_INTENT_TYPE.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return;
            }
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            synchronized (NetworkStateReceive.class) {
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    int type = activeNetwork.getType();
                    if (type != sCurrentNetworkType || !sIsNetworkAvailable) {
                        for (NetworkChangeListener l : sListenerList) {
                            if (type != sCurrentNetworkType) {
                                l.onNetworkTypeChange(type);
                            }
                            if (!sIsNetworkAvailable) {
                                l.onConnectionStateChange(true);
                            }
                        }
                    }
                    sCurrentNetworkType = type;
                    sIsNetworkAvailable = true;
                } else {
                    if (sIsNetworkAvailable) {
                        for (NetworkChangeListener l : sListenerList) {
                            l.onConnectionStateChange(false);
                        }
                    }
                    sIsNetworkAvailable = false;
                }
            }
        }
    }


    public interface NetworkChangeListener {

        void onNetworkTypeChange(int type);

        void onConnectionStateChange(boolean isNetworkAvailable);

    }
}
