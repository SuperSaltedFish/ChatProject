package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.yzx.chat.configure.AppApplication;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class SharePreferenceManager {
    private static final String PREFERENCES_NAME_IDENTITY = "Identity";
    private static final String IDENTITY_KEY_RSA_KEY = "RSA_SecretKey";
    private static final String IDENTITY_KEY_AES_KEY = "AES_SecretKey";
    private static final String IDENTITY_KEY_TOKEN = "Token";
    private static final String IDENTITY_KEY_DEVICE_ID = "DeviceID";

    private static SharePreferenceManager sManager;

    public static SharePreferenceManager getInstance() {
        if (sManager == null) {
            synchronized (IdentityManager.class) {
                if (sManager == null) {
                    sManager = new SharePreferenceManager();
                }
            }
        }
        return sManager;
    }

    private SharedPreferences Identity;

    private SharePreferenceManager() {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
    }

    public SharedPreferences getIdentitySharedPreferences() {
        if (Identity == null) {
            synchronized (this) {
                if (Identity == null) {
                    Identity = AppApplication.getAppContext().getSharedPreferences(PREFERENCES_NAME_IDENTITY, Context.MODE_PRIVATE);
                }
            }
        }
        return Identity;
    }

    public boolean putRSAKey(String value) {
        return getIdentitySharedPreferences().edit().putString(IDENTITY_KEY_RSA_KEY, value).commit();
    }

    public boolean putAESKey(String value) {
        return getIdentitySharedPreferences().edit().putString(IDENTITY_KEY_AES_KEY, value).commit();
    }

    public boolean putToken(String value) {
        return getIdentitySharedPreferences().edit().putString(IDENTITY_KEY_TOKEN, value).commit();
    }

    public void putDeviceID(String value) {
        getIdentitySharedPreferences().edit().putString(IDENTITY_KEY_DEVICE_ID, value).apply();
    }

    public String getRSAKey() {
        return getIdentitySharedPreferences().getString(IDENTITY_KEY_RSA_KEY, null);
    }

    public String getAESKey() {
        return getIdentitySharedPreferences().getString(IDENTITY_KEY_AES_KEY, null);
    }

    public String getToken() {
        return getIdentitySharedPreferences().getString(IDENTITY_KEY_TOKEN, null);
    }

    public String getDeviceID() {
        return getIdentitySharedPreferences().getString(IDENTITY_KEY_DEVICE_ID, null);
    }
}
