package com.yzx.chat.core.manager;

import android.os.Build;
import android.text.TextUtils;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by YZX on 2018年03月06日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class CryptoManager {

    private String mAESKey;

    public CryptoManager(String AESKey) {
        mAESKey = AESKey;
    }

    public byte[] aesEncrypt(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return AESUtil.encrypt(data, mAESKey.getBytes());
    }

    public String aesEncryptToBase64(byte[] data) {
        byte[] encryptData = aesEncrypt(data);
        if (encryptData == null) {
            return null;
        }
        return Base64Util.encodeToString(encryptData);
    }


    public byte[] aesDecrypt(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return AESUtil.decrypt(data, mAESKey.getBytes());
    }

    public byte[] aesDecryptFromBase64String(String base64String) {
        if (TextUtils.isEmpty(base64String)) {
            return null;
        }
        byte[] encryptData = Base64Util.decode(base64String);
        if (encryptData == null) {
            return null;
        }
        return aesDecrypt(encryptData);
    }



    private static final String RSA_KEY_ALIAS = "RSAKey";
    private static String sDeviceID;
    private static KeyPair sRSAKeyPair;
    static {
        sRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(AppApplication.getAppContext(), RSA_KEY_ALIAS);
        sDeviceID = initDeviceID();
    }

    private static synchronized String initDeviceID() {
        if (!TextUtils.isEmpty(sDeviceID)) {
            return sDeviceID;
        }
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        String deviceID = idPref.getDeviceID();
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = String.format(Locale.getDefault(), "Api%d.%s(%s).%s",
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL,
                    UUID.randomUUID());
            byte enDeviceID[] = RSAUtil.encryptByPublicKey(deviceID.getBytes(), sRSAKeyPair.getPublic());
            if (enDeviceID != null && enDeviceID.length != 0) {
                String tmpDeviceID = Base64Util.encodeToString(enDeviceID);
                if (!TextUtils.isEmpty(tmpDeviceID)) {
                    idPref.putDeviceID(tmpDeviceID);
                }
            }
            sDeviceID = deviceID;
            return sDeviceID;
        } else {
            byte enDeviceID[] = Base64Util.decode(deviceID);
            if (enDeviceID != null && enDeviceID.length > 0) {
                enDeviceID = RSAUtil.decryptByPrivateKey(enDeviceID, sRSAKeyPair.getPrivate());
                if (enDeviceID != null && enDeviceID.length > 0) {
                    sDeviceID = new String(enDeviceID);
                    return sDeviceID;
                }
            }
            idPref.clear(true);
            return initDeviceID();
        }
    }

    public static boolean update(String aesKey) {
        if (TextUtils.isEmpty(aesKey)) {
            return false;
        }
        KeyPair rsaKeyPair = CryptoManager.getRSAKeyPair();
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        byte enAESKey[] = RSAUtil.encryptByPublicKey(aesKey.getBytes(), rsaKeyPair.getPublic());
        if (enAESKey == null || enAESKey.length == 0) {
            return false;
        }
        String tmpAESKey = Base64Util.encodeToString(enAESKey);
        if (TextUtils.isEmpty(tmpAESKey)) {
            return false;
        }
        return idPref.putAESKey(tmpAESKey);
    }

     static CryptoManager getInstanceFromLocal() {
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        KeyPair rsaKeyPair = CryptoManager.getRSAKeyPair();
        String strAESKey = idPref.getAESKey();

        if (TextUtils.isEmpty(strAESKey)) {
            return null;
        }
        byte aesKey[] = Base64Util.decode(strAESKey);
        if (aesKey == null || aesKey.length == 0) {
            return null;
        }
        aesKey = RSAUtil.decryptByPrivateKey(aesKey, rsaKeyPair.getPrivate());
        if (aesKey == null || aesKey.length == 0) {
            return null;
        }
        return new CryptoManager(new String(aesKey));
    }

    public static String getDeviceID() {
        return sDeviceID;
    }

    public static String getBase64RSAPublicKey() {
        return Base64Util.encodeToString(sRSAKeyPair.getPublic().getEncoded());
    }

    public static byte[] rsaEncrypt(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return RSAUtil.encryptByPublicKey(data, sRSAKeyPair.getPublic());
    }

    public static String rsaEncryptToBase64String(byte[] data) {
        return Base64Util.encodeToString(rsaEncrypt(data));
    }


    public static byte[] rsaDecrypt(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return RSAUtil.decryptByPrivateKey(data, sRSAKeyPair.getPrivate());
    }

    public static byte[] rsaDecryptFromBase64String(String base64String) {
        return rsaDecrypt(Base64Util.decode(base64String));
    }

    public static KeyPair getRSAKeyPair() {
        return sRSAKeyPair;
    }

}
