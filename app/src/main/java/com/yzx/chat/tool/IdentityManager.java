package com.yzx.chat.tool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
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

    private SharePreferenceManager mSharePreferenceManager;
    private KeyPair mRSAKeyPair;
    private byte[] mAESKey;
    private String mToken;
    private String mDeviceID;


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
        mSharePreferenceManager = SharePreferenceManager.getInstance();
        mRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(AppApplication.getAppContext(), RSA_KEY_ALIAS);
    }

    public synchronized void clearAuthenticationData() {
        mSharePreferenceManager.getIdentitySharedPreferences().edit().clear().apply();
        mAESKey = null;
        mToken = null;
    }

    public void startToLoginActivity(){
        Context context = AppApplication.getAppContext();
        Intent intent = new Intent(context,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isLogged() {
        return !TextUtils.isEmpty(getToken()) && initAESKey();
    }

    public synchronized boolean saveAESKey(String key) {
        byte[] rsaEncrypt = rsaEncryptByPublicKey(key.getBytes());
        if (rsaEncrypt == null) {
            return false;
        }
        String base64Data = Base64Util.encodeToString(rsaEncrypt);
        if (base64Data == null) {
            return false;
        }
        return mSharePreferenceManager.putAESKey(base64Data);
    }

    public boolean saveToken(String token) {
        byte[] encodeData = rsaEncryptByPublicKey(token.getBytes());
        if (encodeData == null) {
            return false;
        }
        String base64Data = Base64Util.encodeToString(encodeData);
        if (base64Data == null) {
            return false;
        }
        return mSharePreferenceManager.putToken(base64Data);
    }

    public String getToken() {
        if (mToken == null && !checkHasToken()) {
            return null;
        }
        return mToken;
    }

    public String getDeviceID() {
        if (mDeviceID == null) {
            createDeviceID();
        }
        return mDeviceID;
    }

    public String getUserID() {
        return "244546875";
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
            String aesKeyStr = mSharePreferenceManager.getAESKey();
            if (aesKeyStr == null) {
                return false;
            }
            mAESKey = rsaDecryptByPrivateKey(Base64Util.decode(aesKeyStr));
        }
        return mAESKey != null;
    }

    private synchronized boolean checkHasToken() {
        if (mToken == null) {
            String tokenStr = mSharePreferenceManager.getToken();
            if (tokenStr != null) {
                byte[] tokenBytes = rsaDecryptByPrivateKey(Base64Util.decode(tokenStr));
                if (tokenBytes != null) {
                    mToken = new String(rsaDecryptByPrivateKey(Base64Util.decode(tokenStr)));
                }
            }
        }
        return mToken != null;
    }

    private synchronized void createDeviceID() {
        mDeviceID = mSharePreferenceManager.getDeviceID();
        if (mDeviceID != null) {
            byte[] data = rsaDecryptByPrivateKey(Base64Util.decode(mDeviceID));
            if (data != null) {
                mDeviceID = new String(data);
            } else {
                mDeviceID = null;
            }
        }
        if (mDeviceID == null) {
            mDeviceID = String.format(Locale.getDefault(), "%s.%d.Api%s(%s)",
                    UUID.randomUUID(),
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL);
            String base64DeviceID = Base64Util.encodeToString(rsaEncryptByPublicKey(mDeviceID.getBytes()));
            mSharePreferenceManager.putDeviceID(base64DeviceID);
        }
    }

}
