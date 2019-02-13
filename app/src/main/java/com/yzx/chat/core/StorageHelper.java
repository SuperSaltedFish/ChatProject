package com.yzx.chat.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yzx.chat.core.util.Base64Util;
import com.yzx.chat.core.util.ECCUtil;
import com.yzx.chat.core.util.LogUtil;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

/**
 * Created by 叶智星 on 2018年09月18日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
@SuppressLint("ApplySharedPref")
class StorageHelper {

    private static final String RSA_KET_ALIAS = StorageHelper.class.getName() + ".ECC";
    private static final String STORAGE_KEY_TOKEN = "Token";
    private static final String STORAGE_KEY_USER_INFO = "UserInfo";
    private static final String STORAGE_KEY_DEVICE_ID = "DeviceID";

    private SharedPreferences mConfigurationPreferences;
    private KeyPair mECCKeyPair;

    StorageHelper(Context appContent, String storageName) {
        mConfigurationPreferences = appContent.getSharedPreferences(storageName, Context.MODE_PRIVATE);
        mECCKeyPair = ECCUtil.generateECCKeyPairInAndroidKeyStore(appContent, RSA_KET_ALIAS);
        if (mECCKeyPair == null) {
            mECCKeyPair = ECCUtil.generateECCKeyPairByBC();
        }
    }

    boolean saveToken(String token) {
        return putToConfigurationPreferences(STORAGE_KEY_TOKEN, token);
    }

    String getToken() {
        return getFromConfigurationPreferences(STORAGE_KEY_TOKEN);
    }

    boolean saveUserInfo(String userInfo) {
        return putToConfigurationPreferences(STORAGE_KEY_USER_INFO, userInfo);
    }

    String getUserInfo() {
        return getFromConfigurationPreferences(STORAGE_KEY_USER_INFO);
    }

    boolean saveDeviceID(String deviceID) {
        return putToConfigurationPreferences(STORAGE_KEY_DEVICE_ID, deviceID);
    }

    String getDeviceID() {
        return getFromConfigurationPreferences(STORAGE_KEY_DEVICE_ID);
    }

    boolean putToConfigurationPreferences(String key, String value) {
        if (!TextUtils.isEmpty(value) && mECCKeyPair != null) {
            byte[] data = ECCUtil.encryptByPublicKey(value.getBytes(Charset.defaultCharset()), (ECPublicKey) mECCKeyPair.getPublic());
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
        if (!TextUtils.isEmpty(value) && mECCKeyPair != null) {
            byte[] data = ECCUtil.decryptByPrivateKey(Base64Util.decode(value), mECCKeyPair.getPrivate());
            if (data != null && data.length > 0) {
                value = new String(data, Charset.defaultCharset());
            }
        }
        return value;
    }

    void clear() {
        String deviceID = getDeviceID();
        mConfigurationPreferences.edit().clear().commit();
        saveDeviceID(deviceID);
    }
}
