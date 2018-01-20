package com.yzx.chat.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.view.activity.LoginActivity;

import java.security.KeyPair;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class IdentityManager {

    private static final String RSA_KEY_ALIAS = "RSAKey";

    private static volatile IdentityManager sManager;

    private SharePreferenceManager.IdentityPreferences mIdentityPreferences;
    private KeyPair mRSAKeyPair;
    private byte[] mAESKey;
    private String mToken;
    private String mDeviceID;
    private String mUserID;


    public static IdentityManager getInstance() {
        if (sManager == null) {
            synchronized (IdentityManager.class) {
                if (sManager == null) {
                    sManager = new IdentityManager();
                }
            }
        }
        return sManager;
    }

    private IdentityManager() {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mIdentityPreferences = SharePreferenceManager.getInstance().getIdentityPreferences();
        mRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(AppApplication.getAppContext(), RSA_KEY_ALIAS);
        createDeviceID();
    }

    public synchronized void clearAuthenticationData() {
        mIdentityPreferences.clear();
        mAESKey = null;
        mToken = null;
    }

    public void startToLoginActivity() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = AppApplication.getAppContext();
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    public boolean isLogged() {
        return !TextUtils.isEmpty(getToken()) && initAESKey();
    }

    public synchronized boolean saveAESKey(String key) {
        String encryptData = encrypt(key);
        return !TextUtils.isEmpty(encryptData) && mIdentityPreferences.putAESKey(encryptData);
    }

    public boolean saveUserID(String userID) {
        String encryptData = encrypt(userID);
        return !TextUtils.isEmpty(encryptData) && mIdentityPreferences.putUserID(encryptData);
    }

    public boolean saveToken(String token) {
        String encryptData = encrypt(token);
        return !TextUtils.isEmpty(encryptData) && mIdentityPreferences.putToken(encryptData);
    }

    public String getToken() {
        if (TextUtils.isEmpty(mToken)) {
            mToken = mIdentityPreferences.getToken();
            mToken = decrypt(mToken);
        }
        if (TextUtils.isEmpty(mToken)) {
            startToLoginActivity();
        }
        return mToken;
    }

    public String getDeviceID() {
        if (mDeviceID == null) {
            createDeviceID();
        }
        return mDeviceID;
    }

    @Nullable
    public String getUserID() {
        if (TextUtils.isEmpty(mUserID)) {
            mUserID = mIdentityPreferences.getUserID();
            mUserID = decrypt(mUserID);
        }
        if (TextUtils.isEmpty(mUserID)) {
            startToLoginActivity();
        }
        return mUserID;
    }

    public String getBase64RSAPublicKey() {
        return Base64Util.encodeToString(mRSAKeyPair.getPublic().getEncoded());
    }


    public byte[] rsaEncryptByPublicKey(byte[] data) {
        return RSAUtil.encryptByPublicKey(data, mRSAKeyPair.getPublic());
    }

    public byte[] rsaDecryptByPrivateKey(byte[] data) {
        return RSAUtil.decryptByPrivateKey(data, mRSAKeyPair.getPrivate());
    }

    public byte[] aesEncrypt(byte[] data) {
        if (!initAESKey()) {
            return null;
        }
        return AESUtil.encrypt(data, mAESKey);
    }

    public String aesEncryptToBase64(byte[] data) {
        byte[] encryptData = aesEncrypt(data);
        if (encryptData == null) {
            return null;
        }
        return Base64Util.encodeToString(encryptData);
    }


    public byte[] aesDecrypt(byte[] data) {
        if (!initAESKey()) {
            return null;
        }
        return AESUtil.decrypt(data, mAESKey);
    }

    public byte[] aesDecryptFromBase64String(String base64String) {
        byte[] encryptData = Base64Util.decode(base64String);
        if (encryptData == null) {
            return null;
        }
        return aesDecrypt(encryptData);
    }

    private synchronized boolean initAESKey() {
        if (mAESKey == null) {
            String aesKeyStr = mIdentityPreferences.getAESKey();
            if (aesKeyStr == null) {
                return false;
            }
            mAESKey = rsaDecryptByPrivateKey(Base64Util.decode(aesKeyStr));
        }
        return mAESKey != null;
    }

    private synchronized void createDeviceID() {
        SharePreferenceManager.ConfigurePreferences preferences = SharePreferenceManager.getInstance().getConfigurePreferences();
        mDeviceID = preferences.getDeviceID();
        if (TextUtils.isEmpty(mDeviceID)) {
            mDeviceID = String.format(Locale.getDefault(), "%s.%d.Api%s(%s)",
                    UUID.randomUUID(),
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL);
            String encryptDeviceID = encrypt(mDeviceID);
            preferences.putDeviceID(encryptDeviceID);
        } else {
            mDeviceID = decrypt(mDeviceID);
            if (TextUtils.isEmpty(mDeviceID)) {
                mDeviceID = null;
                createDeviceID();
            }
        }
    }

    private String encrypt(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        byte[] data = rsaEncryptByPublicKey(value.getBytes());
        if (data == null) {
            return null;
        }
        return Base64Util.encodeToString(data);
    }

    private String decrypt(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        byte[] data = Base64Util.decode(value);
        if (data == null) {
            return null;
        }
        data = rsaDecryptByPrivateKey(data);
        if (data != null) {
            return new String(data);
        }
        return null;
    }

}
