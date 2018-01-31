package com.yzx.chat.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.database.UserDao;
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

    private static IdentityManager sManager;

    public static IdentityManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("The IdentityManager no initialization.");
        }
        return sManager;
    }

    public synchronized static boolean init(Context context, String token, String aesKey, UserBean user) {
        if (sManager != null) {
            return true;
        }

        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(aesKey) || user == null || user.isEmpty()) {
            return false;
        }

        KeyPair rsaKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(context, RSA_KEY_ALIAS);
        if (rsaKeyPair == null) {
            return false;
        }
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();


        byte enToken[] = RSAUtil.encryptByPublicKey(token.getBytes(), rsaKeyPair.getPublic());
        if (enToken == null || enToken.length == 0) {
            return false;
        }
        String tmpToken = Base64Util.encodeToString(enToken);
        if (TextUtils.isEmpty(tmpToken)) {
            return false;
        }

        byte enAESKey[] = RSAUtil.encryptByPublicKey(aesKey.getBytes(), rsaKeyPair.getPublic());
        if (enAESKey == null || enAESKey.length == 0) {
            return false;
        }
        String tmpAESKey = Base64Util.encodeToString(enAESKey);
        if (TextUtils.isEmpty(tmpAESKey)) {
            return false;
        }

        String tmpUserID = user.getUserID();
        byte enUserID[] = RSAUtil.encryptByPublicKey(tmpUserID.getBytes(), rsaKeyPair.getPublic());
        if (enUserID == null || enUserID.length == 0) {
            return false;
        }
        tmpUserID = Base64Util.encodeToString(enUserID);
        if (TextUtils.isEmpty(tmpUserID)) {
            return false;
        }

        if (!idPref.putToken(tmpToken) || !idPref.putAESKey(tmpAESKey) || !idPref.putUserID(tmpUserID)) {
            idPref.clear(false);
            return false;
        }
        sManager = new IdentityManager(token, aesKey, initDeviceID(idPref, rsaKeyPair), user, rsaKeyPair);
        return true;
    }

    public synchronized static boolean initFromLocal(Context context) {
        if (sManager != null) {
            return true;
        }
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        String strToken = idPref.getToken();
        String strAESKey = idPref.getAESKey();
        String strDeviceID = idPref.getDeviceID();
        String strUserID = idPref.getUserID();

        if (TextUtils.isEmpty(strToken) || TextUtils.isEmpty(strAESKey) || TextUtils.isEmpty(strDeviceID) || TextUtils.isEmpty(strUserID)) {
            return false;
        }

        KeyPair rsaKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(context, RSA_KEY_ALIAS);
        if (rsaKeyPair == null) {
            return false;
        }

        byte token[] = Base64Util.decode(strToken);
        if (token == null || token.length == 0) {
            return false;
        }
        token = RSAUtil.decryptByPrivateKey(token, rsaKeyPair.getPrivate());
        if (token == null || token.length == 0) {
            return false;
        }

        byte aesKey[] = Base64Util.decode(strAESKey);
        if (aesKey == null || aesKey.length == 0) {
            return false;
        }
        aesKey = RSAUtil.decryptByPrivateKey(aesKey, rsaKeyPair.getPrivate());
        if (aesKey == null || aesKey.length == 0) {
            return false;
        }

        byte deviceID[] = Base64Util.decode(strDeviceID);
        if (deviceID == null || deviceID.length == 0) {
            return false;
        }
        deviceID = RSAUtil.decryptByPrivateKey(deviceID, rsaKeyPair.getPrivate());
        if (deviceID == null || deviceID.length == 0) {
            return false;
        }

        byte userID[] = Base64Util.decode(strUserID);
        if (userID == null || userID.length == 0) {
            return false;
        }
        userID = RSAUtil.decryptByPrivateKey(userID, rsaKeyPair.getPrivate());
        if (userID == null || userID.length == 0) {
            return false;
        }


        strUserID = new String(userID);
        UserBean user = DBManager.getInstance().getUserDao().loadByKey(strUserID);
        if (user == null || user.isEmpty()) {
            return false;
        }

        sManager = new IdentityManager(new String(token), new String(aesKey), new String(deviceID), user, rsaKeyPair);
        return true;
    }

    private static String initDeviceID(SharePreferenceManager.IdentityPreferences idPref, KeyPair rsaKeyPair) {
        String deviceID = idPref.getDeviceID();
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = String.format(Locale.getDefault(), "%s.%d.Api%s(%s)",
                    UUID.randomUUID(),
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL);
            byte enDeviceID[] = RSAUtil.encryptByPublicKey(deviceID.getBytes(), rsaKeyPair.getPublic());
            if (enDeviceID != null && enDeviceID.length != 0) {
                String tmpDeviceID = Base64Util.encodeToString(enDeviceID);
                if (!TextUtils.isEmpty(tmpDeviceID)) {
                    idPref.putDeviceID(tmpDeviceID);
                }
            }
            return deviceID;
        } else {
            byte enDeviceID[] = Base64Util.decode(deviceID);
            if (enDeviceID != null && enDeviceID.length > 0) {
                enDeviceID = RSAUtil.decryptByPrivateKey(enDeviceID, rsaKeyPair.getPrivate());
                if (enDeviceID != null && enDeviceID.length > 0) {
                    deviceID = new String(enDeviceID);
                    return deviceID;
                }
            }
            idPref.clear(true);
            return initDeviceID(idPref, rsaKeyPair);
        }
    }


    private String mToken;
    private String mAESKey;
    private String mDeviceID;
    private UserBean mUserBean;
    private KeyPair mRSAKeyPair;

    private IdentityManager(String token, String AESKey, String deviceID, UserBean userBean, KeyPair RSAKeyPair) {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mToken = token;
        mAESKey = AESKey;
        mDeviceID = deviceID;
        mUserBean = userBean;
        mRSAKeyPair = RSAKeyPair;
    }


    public void logout() {
        SharePreferenceManager.getIdentityPreferences().clear(false);
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
        return !TextUtils.isEmpty(getToken()) && initAESKey() && mUserBean != null && !mUserBean.isEmpty();
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
        if (mUserBean == null || TextUtils.isEmpty(mUserBean.getUserID())) {
            startToLoginActivity();
            return "";
        } else {
            return mUserBean.getUserID();
        }
    }

    public UserBean getUser() {
        return mUserBean;
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

    private boolean saveUserID(String userID) {
        String encryptData = encrypt(userID);
        return !TextUtils.isEmpty(encryptData) && mIdentityPreferences.putUserID(encryptData);
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
