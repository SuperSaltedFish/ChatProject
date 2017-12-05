package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.yzx.chat.configure.AppApplication;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class SharePreferenceManager {

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

    private IdentityPreferences mIdentityPreferences;
    private ConfigurePreferences mConfigurePreferences;

    private SharePreferenceManager() {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
    }

    public IdentityPreferences getIdentityPreferences() {
        if (mIdentityPreferences == null) {
            synchronized (this) {
                if (mIdentityPreferences == null) {
                    mIdentityPreferences = new IdentityPreferences(AppApplication.getAppContext());
                }
            }
        }
        return mIdentityPreferences;
    }

    public ConfigurePreferences getConfigurePreferences() {
        if (mConfigurePreferences == null) {
            synchronized (this) {
                if (mConfigurePreferences == null) {
                    mConfigurePreferences = new ConfigurePreferences(AppApplication.getAppContext());
                }
            }
        }
        return mConfigurePreferences;
    }


    public static class IdentityPreferences {
        private static final String PREFERENCES_NAME_IDENTITY = "Identity";
        private static final String IDENTITY_KEY_RSA_Secret_Key = "RSASecretKey";
        private static final String IDENTITY_KEY_AES_Secret_Key = "AESSecretKey";
        private static final String IDENTITY_KEY_Token = "Token";
        private static final String IDENTITY_KEY_DEVICE_ID = "DeviceID";

        private SharedPreferences mPreferences;

        IdentityPreferences(Context context) {
            mPreferences = context.getSharedPreferences(PREFERENCES_NAME_IDENTITY, Context.MODE_PRIVATE);
        }

        public void clear() {
            String deviceID = getDeviceID();
            mPreferences.edit().clear().apply();
            putDeviceID(deviceID);
        }

        public boolean putRSAKey(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_RSA_Secret_Key, value).commit();
        }

        public boolean putAESKey(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_AES_Secret_Key, value).commit();
        }

        public boolean putToken(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_Token, value).commit();
        }

        public boolean putDeviceID(String value) {
           return mPreferences.edit().putString(IDENTITY_KEY_DEVICE_ID, value).commit();
        }

        public String getRSAKey() {
            return mPreferences.getString(IDENTITY_KEY_RSA_Secret_Key, null);
        }

        public String getAESKey() {
            return mPreferences.getString(IDENTITY_KEY_AES_Secret_Key, null);
        }

        public String getToken() {
            return mPreferences.getString(IDENTITY_KEY_Token, null);
        }

        public String getDeviceID() {
            return mPreferences.getString(IDENTITY_KEY_DEVICE_ID, null);
        }
    }

    public static class ConfigurePreferences {
        private static final String PREFERENCES_NAME_CONFIGURE = "Configure";
        private static final String CONFIGURE_KEY_KEY_BOARD_HEIGHT = "KeyBoardHeight";

        private SharedPreferences mPreferences;

        ConfigurePreferences(Context context) {
            mPreferences = context.getSharedPreferences(PREFERENCES_NAME_CONFIGURE, Context.MODE_PRIVATE);
        }

        public void putKeyBoardHeight(int value) {
            mPreferences.edit().putInt(CONFIGURE_KEY_KEY_BOARD_HEIGHT, value).apply();
        }

        public int getKeyBoardHeight() {
            return mPreferences.getInt(CONFIGURE_KEY_KEY_BOARD_HEIGHT, 0);
        }
    }
}
