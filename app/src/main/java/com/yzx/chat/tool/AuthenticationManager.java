package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;

import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AuthenticationManager {

    private static volatile AuthenticationManager sManager;

    private Context mAppContext;
    private SharedPreferences mPreferences;
    private KeyPair mRSAKeyPair;
    private String mAESKeyAlias;
    private String mTokenAlias;
    private String mDeviceIDAlias;
    private byte[] mAESKey;
    private String mToken;
    private String mDeviceID;

    public synchronized static void init(Context applicationContext, String preferencesFileName,
                                         String rsaKeyAlias, String aesKeyAlias, String tokenAlias, String deviceIDAlias) {
        sManager = new AuthenticationManager(applicationContext, preferencesFileName, rsaKeyAlias, aesKeyAlias, tokenAlias, deviceIDAlias);
    }

    public static AuthenticationManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("AuthenticationManager is not initialized");
        }
        return sManager;
    }

    private AuthenticationManager(Context applicationContext, String preferencesFileName,
                                  String rsaKeyAlias, String aesKeyAlias, String tokenAlias, String deviceIDAlias) {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mAppContext = applicationContext;
        mAESKeyAlias = aesKeyAlias;
        mTokenAlias = tokenAlias;
        mDeviceIDAlias = deviceIDAlias;
        mPreferences = mAppContext.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        mRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(rsaKeyAlias);
    }

    public synchronized void clearAuthenticationData() {
        mPreferences.edit().clear().apply();
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

    public String getBase64RSAPublicKey() {
        return Base64.encodeToString(mRSAKeyPair.getPublic().getEncoded(), Base64.DEFAULT);
    }

    public synchronized boolean saveAESKey(byte[] key) {
        byte[] rsaEncrypt = rsaEncryptByPublicKey(key);
        if (rsaEncrypt == null) {
            return false;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mAESKeyAlias, Base64.encodeToString(rsaEncrypt, Base64.DEFAULT));
        return editor.commit();
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
        return Base64.encodeToString(encryptData, Base64.DEFAULT);
    }


    public byte[] aesDecrypt(byte[] data) {
        if (!initAESKey()) {
            return null;
        }
        return AESUtil.decrypt(data, mAESKey);
    }

    public byte[] aesDecryptFromBase64String(String base64String) {
        byte[] encryptData = Base64.decode(base64String, Base64.DEFAULT);
        if (encryptData == null) {
            return null;
        }
        return aesDecrypt(encryptData);
    }

    private synchronized boolean initAESKey() {
        if (mAESKey == null) {
            String aesKeyStr = mPreferences.getString(mAESKeyAlias, null);
            if (aesKeyStr == null) {
                return false;
            }
            mAESKey = rsaDecryptByPrivateKey(Base64.decode(aesKeyStr, Base64.DEFAULT));
        }
        return mAESKey != null;
    }

    private synchronized boolean checkHasToken() {
        if (mToken == null) {
            String tokenStr = mPreferences.getString(mTokenAlias, null);
            if (tokenStr != null) {
                byte[] tokenBytes = rsaDecryptByPrivateKey(Base64.decode(tokenStr, Base64.DEFAULT));
                if (tokenBytes != null) {
                    mToken = new String(rsaDecryptByPrivateKey(Base64.decode(tokenStr, Base64.DEFAULT)));
                }
            }
        }
        return mToken != null;
    }

    private synchronized void createDeviceID() {
        mDeviceID = mPreferences.getString(mDeviceIDAlias, null);
        if (mDeviceID != null) {
            byte[] data = rsaDecryptByPrivateKey(Base64.decode(mDeviceID, Base64.DEFAULT));
            if (data != null) {
                mDeviceID = new String(data);
            }else {
                mDeviceID=null;
            }
        }
        if (mDeviceID == null) {
            mDeviceID = String.format(Locale.getDefault(), "%s.%d.Api%s(%s)",
                    UUID.randomUUID(),
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL);
            String base64DeviceID = Base64.encodeToString(rsaEncryptByPublicKey(mDeviceID.getBytes()), Base64.DEFAULT);
            mPreferences.edit().putString(mDeviceIDAlias, base64DeviceID).apply();
        }
    }

}
