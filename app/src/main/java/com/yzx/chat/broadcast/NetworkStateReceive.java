package com.yzx.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;

import com.yzx.chat.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by YZX on 2017年10月23日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class NetworkStateReceive extends BroadcastReceiver{

    private static final String RECEIVE_INTENT_TYPE = ConnectivityManager.CONNECTIVITY_ACTION;


    private static Map<Context,NetworkStateReceive> sReceiveMap = new WeakHashMap<>();

    public static void register(Context context,OnStateChangeListener listener){
        if(context==null||listener==null||sReceiveMap.get(context)!=null){
            return;
        }

        IntentFilter filter = new IntentFilter(RECEIVE_INTENT_TYPE);
        NetworkStateReceive receive = new NetworkStateReceive(listener);
        context.registerReceiver(receive,filter);
        sReceiveMap.put(context,receive);
    }

    public static void unregister(Context context){
        if(context==null){
            return;
        }
        NetworkStateReceive receive = sReceiveMap.get(context);
        if(receive!=null){
            context.unregisterReceiver(receive);
            receive.destroy();
            sReceiveMap.remove(context);
        }
    }


    private OnStateChangeListener mChangeListener;

    public NetworkStateReceive(OnStateChangeListener changeListener) {
        mChangeListener = changeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (RECEIVE_INTENT_TYPE.equals(intent.getAction())) {
            if(mChangeListener==null){
                return;
            }
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(manager==null){
                return ;
            }
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    LogUtil.e("当前WiFi连接可用 ");
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    LogUtil.e("当前移动网络连接可用 ");
                }
                mChangeListener.onConnectionStatechange(true,activeNetwork.getType());
            } else {
                mChangeListener.onConnectionStatechange(false,-1);
                LogUtil.e("当前没有网络，请确保你已经打开网络连接");
            }
        }
    }

    public void destroy(){
        mChangeListener=null;
    }

    public interface OnStateChangeListener {

       void onConnectionStatechange(boolean isNetworkAvailable,int type);

    }
}
