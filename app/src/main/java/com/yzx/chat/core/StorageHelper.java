package com.yzx.chat.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yzx.chat.core.util.AESUtil;
import com.yzx.chat.core.util.Base64Util;
import com.yzx.chat.core.util.LogUtil;

import javax.crypto.SecretKey;

/**
 * Created by 叶智星 on 2018年09月18日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
@SuppressLint("ApplySharedPref")
class StorageHelper {

    private static final String AES_KET_ALIAS = StorageHelper.class.getName() + ".AES";

    private static final String STORAGE_KEY_TOKEN = "Token";
    private static final String STORAGE_KEY_USER_ID = "UserID";
    private static final String STORAGE_KEY_DEVICE_ID = "DeviceID";

    private SharedPreferences mConfigurationPreferences;
    private SecretKey mAESKey;
    private byte[] mAesIV;

    StorageHelper(Context appContent, String storageName) {
        mConfigurationPreferences = appContent.getSharedPreferences(storageName, Context.MODE_PRIVATE);
        mAESKey = AESUtil.generateAESKeyInAndroidKeyStoreApi23(AES_KET_ALIAS, 192);
        if (mAESKey == null) {
            mAESKey = AESUtil.generateAESKey(192);
        }
        mAesIV = new byte[16];
    }

    boolean saveToken(String token) {
        return putToConfigurationPreferences(STORAGE_KEY_TOKEN, token);
    }

    String getToken() {
        return getFromConfigurationPreferences(STORAGE_KEY_TOKEN);
    }

    boolean saveUserID(String userInfo) {
        return putToConfigurationPreferences(STORAGE_KEY_USER_ID, userInfo);
    }

    String getUserID() {
        return getFromConfigurationPreferences(STORAGE_KEY_USER_ID);
    }

    boolean saveDeviceID(String deviceID) {
        return putToConfigurationPreferences(STORAGE_KEY_DEVICE_ID, deviceID);
    }

    String getDeviceID() {
        return getFromConfigurationPreferences(STORAGE_KEY_DEVICE_ID);
    }

    boolean putToConfigurationPreferences(String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            byte[] data = AESUtil.encrypt(value.getBytes(), mAESKey, mAesIV);
            if (data != null && data.length > 0) {
                value = Base64Util.encodeToString(data);
            } else {
                LogUtil.w("Encrypt fail,key=" + key);
                return false;
            }
        }
        return mConfigurationPreferences.edit().putString(key, value).commit();
    }

    String getFromConfigurationPreferences(String key) {
        String value = mConfigurationPreferences.getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            byte[] data = AESUtil.decrypt(Base64Util.decode(value), mAESKey, mAesIV);
            if (data != null && data.length > 0) {
              return new String(data);
            }
        }
        return null;
    }

    void clear() {
        String deviceID = getDeviceID();
        mConfigurationPreferences.edit().clear().commit();
        saveDeviceID(deviceID);
    }
}
