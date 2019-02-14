package com.yzx.chat.core.net.framework.Executor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yzx.chat.core.util.LogUtil;

import androidx.core.content.ContextCompat;

/**
 * Created by YZX on 2018年06月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NetworkUtil {

    private static boolean isHasNetworkAccessPermission;

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            if (!isHasNetworkAccessPermission) {
                isHasNetworkAccessPermission = checkNetworkAccessPermission(context);
            }
            if (isHasNetworkAccessPermission) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
                    if (mNetworkInfo != null) {
                        return mNetworkInfo.isAvailable();
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkNetworkAccessPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.e("Lack of 'Manifest.permission.INTERNET' permission");
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.e("Lack of 'Manifest.permission.ACCESS_NETWORK_STATE' permission");
            return false;
        }
        return true;
    }

}
