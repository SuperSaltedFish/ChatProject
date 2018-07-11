package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.AndroidUtil;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class SharePreferenceManager {

    private static IdentityPreferences mIdentityPreferences;
    private static ConfigurePreferences mConfigurePreferences;


    public static IdentityPreferences getIdentityPreferences() {
        if (mIdentityPreferences == null) {
            synchronized (SharePreferenceManager.class) {
                if (mIdentityPreferences == null) {
                    mIdentityPreferences = new IdentityPreferences(AppApplication.getAppContext());
                }
            }
        }
        return mIdentityPreferences;
    }

    public static ConfigurePreferences getConfigurePreferences() {
        if (mConfigurePreferences == null) {
            synchronized (SharePreferenceManager.class) {
                if (mConfigurePreferences == null) {
                    mConfigurePreferences = new ConfigurePreferences(AppApplication.getAppContext());
                }
            }
        }
        return mConfigurePreferences;
    }


    public static class IdentityPreferences {
        private static final String PREFERENCES_NAME_IDENTITY = "Identity";
        private static final String IDENTITY_KEY_AES_Secret_Key = "AESSecretKey";
        private static final String IDENTITY_KEY_Token = "Token";
        private static final String IDENTITY_KEY_USER_ID = "UserID";
        private static final String IDENTITY_KEY_DEVICE_ID = "DeviceID";

        private SharedPreferences mPreferences;

        IdentityPreferences(Context context) {
            mPreferences = context.getSharedPreferences(PREFERENCES_NAME_IDENTITY, Context.MODE_PRIVATE);
        }

        public void clear(boolean isClearDeviceID) {
            if (isClearDeviceID) {
                mPreferences.edit().clear().apply();
            } else {
                String deviceID = getDeviceID();
                mPreferences.edit().clear().commit();
                putDeviceID(deviceID);
            }
        }

        public boolean putUserID(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_USER_ID, value).commit();
        }

        public boolean putAESKey(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_AES_Secret_Key, value).commit();
        }

        public boolean putToken(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_Token, value).commit();
        }

        public String getUserID() {
            return mPreferences.getString(IDENTITY_KEY_USER_ID, null);
        }

        public String getAESKey() {
            return mPreferences.getString(IDENTITY_KEY_AES_Secret_Key, null);
        }

        public String getToken() {
            return mPreferences.getString(IDENTITY_KEY_Token, null);
        }

        public boolean putDeviceID(String value) {
            return mPreferences.edit().putString(IDENTITY_KEY_DEVICE_ID, value).commit();
        }

        public String getDeviceID() {
            return mPreferences.getString(IDENTITY_KEY_DEVICE_ID, null);
        }
    }

    public static class ConfigurePreferences {
        private static final String PREFERENCES_NAME_CONFIGURE = "Configure";
        private static final String CONFIGURE_KEY_KEY_BOARD_HEIGHT = "KeyBoardHeight";
        private static final String CONFIGURE_KEY_IS_FIRST_GUIDE = "IsFirstGuide";

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

        public void putFirstGuide(boolean value) {
            mPreferences.edit().putBoolean(CONFIGURE_KEY_IS_FIRST_GUIDE, value).apply();
        }

        public boolean isFirstGuide() {
            return mPreferences.getBoolean(CONFIGURE_KEY_IS_FIRST_GUIDE, true);
        }

    }
}
