package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class IdentityManager {

    private static volatile IdentityManager sManager;

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

        sManager = new IdentityManager(applicationContext, preferencesFileName, rsaKeyAlias, aesKeyAlias, tokenAlias, deviceIDAlias);
    }

    public static IdentityManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("AuthenticationManager is not initialized");
        }
        return sManager;
    }

    private IdentityManager(Context applicationContext, String preferencesFileName,
                            String rsaKeyAlias, String aesKeyAlias, String tokenAlias, String deviceIDAlias) {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mAppContext = applicationContext;
        mAESKeyAlias = aesKeyAlias;
        mTokenAlias = tokenAlias;
        mDeviceIDAlias = deviceIDAlias;
        mPreferences = mAppContext.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        mRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(mAppContext,rsaKeyAlias);
    }

    public synchronized void clearAuthenticationData() {
        mPreferences.edit().clear().apply();
        mAESKey = null;
        mToken = null;
    }

    public boolean isLogged(){
        return !TextUtils.isEmpty(getToken()) && initAESKey();
    }

    public synchronized boolean saveAESKey(String key) {
        byte[] rsaEncrypt = rsaEncryptByPublicKey(key.getBytes());
        if (rsaEncrypt == null) {
            return false;
        }
        String base64Data = Base64Util.encodeToString(rsaEncrypt);
        if(base64Data==null){
            return false;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mAESKeyAlias, base64Data);
        return editor.commit();
    }

    public boolean saveToken(String token){
        byte[] encodeData = rsaEncryptByPublicKey(token.getBytes());
        if(encodeData==null){
            return false;
        }
        String base64Data = Base64Util.encodeToString(encodeData);
        if(base64Data==null){
            return false;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mTokenAlias,base64Data);
        return editor.commit();
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

    public String getUserID(){
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
            String aesKeyStr = mPreferences.getString(mAESKeyAlias, null);
            if (aesKeyStr == null) {
                return false;
            }
            mAESKey = rsaDecryptByPrivateKey(Base64Util.decode(aesKeyStr));
        }
        return mAESKey != null;
    }

    private synchronized boolean checkHasToken() {
        if (mToken == null) {
            String tokenStr = mPreferences.getString(mTokenAlias, null);
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
        mDeviceID = mPreferences.getString(mDeviceIDAlias, null);
        if (mDeviceID != null) {
            byte[] data = rsaDecryptByPrivateKey(Base64Util.decode(mDeviceID));
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
            String base64DeviceID = Base64Util.encodeToString(rsaEncryptByPublicKey(mDeviceID.getBytes()));
            mPreferences.edit().putString(mDeviceIDAlias, base64DeviceID).apply();
        }
    }

}
