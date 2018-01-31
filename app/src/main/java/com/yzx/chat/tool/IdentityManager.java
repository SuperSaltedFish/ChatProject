package com.yzx.chat.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yzx.chat.bean.UserBean;
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

    private static IdentityManager sManager;
    private static String sDeviceID;
    private static KeyPair sRSAKeyPair;

    static {
        sRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(AppApplication.getAppContext(), RSA_KEY_ALIAS);
        sDeviceID = initDeviceID();
    }

    public static IdentityManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("The IdentityManager no initialization.");
        }
        return sManager;
    }

    public synchronized static boolean init(String token, String aesKey, UserBean user) {
        if (sManager != null) {
            return true;
        }

        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(aesKey) || user == null || user.isEmpty()) {
            return false;
        }


        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();

        byte enToken[] = RSAUtil.encryptByPublicKey(token.getBytes(), sRSAKeyPair.getPublic());
        if (enToken == null || enToken.length == 0) {
            return false;
        }
        String tmpToken = Base64Util.encodeToString(enToken);
        if (TextUtils.isEmpty(tmpToken)) {
            return false;
        }

        byte enAESKey[] = RSAUtil.encryptByPublicKey(aesKey.getBytes(), sRSAKeyPair.getPublic());
        if (enAESKey == null || enAESKey.length == 0) {
            return false;
        }
        String tmpAESKey = Base64Util.encodeToString(enAESKey);
        if (TextUtils.isEmpty(tmpAESKey)) {
            return false;
        }

        String tmpUserID = user.getUserID();
        byte enUserID[] = RSAUtil.encryptByPublicKey(tmpUserID.getBytes(), sRSAKeyPair.getPublic());
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
        if (!DBManager.getInstance().getUserDao().insert(user)) {
            return false;
        }
        sManager = new IdentityManager(token, aesKey, user);
        return true;
    }

    public synchronized static boolean initFromLocal() {
        if (sManager != null) {
            return true;
        }
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        String strToken = idPref.getToken();
        String strAESKey = idPref.getAESKey();
        String strUserID = idPref.getUserID();

        if (TextUtils.isEmpty(strToken) || TextUtils.isEmpty(strAESKey) || TextUtils.isEmpty(strUserID)) {
            return false;
        }

        byte token[] = Base64Util.decode(strToken);
        if (token == null || token.length == 0) {
            return false;
        }
        token = RSAUtil.decryptByPrivateKey(token, sRSAKeyPair.getPrivate());
        if (token == null || token.length == 0) {
            return false;
        }

        byte aesKey[] = Base64Util.decode(strAESKey);
        if (aesKey == null || aesKey.length == 0) {
            return false;
        }
        aesKey = RSAUtil.decryptByPrivateKey(aesKey, sRSAKeyPair.getPrivate());
        if (aesKey == null || aesKey.length == 0) {
            return false;
        }

        byte userID[] = Base64Util.decode(strUserID);
        if (userID == null || userID.length == 0) {
            return false;
        }
        userID = RSAUtil.decryptByPrivateKey(userID, sRSAKeyPair.getPrivate());
        if (userID == null || userID.length == 0) {
            return false;
        }

        UserBean user = DBManager.getInstance().getUserDao().loadByKey(new String(userID));
        if (user == null || user.isEmpty()) {
            return false;
        }

        sManager = new IdentityManager(new String(token), new String(aesKey), user);
        return true;
    }

    private static synchronized String initDeviceID() {
        if (!TextUtils.isEmpty(sDeviceID)) {
            return sDeviceID;
        }
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        String deviceID = idPref.getDeviceID();
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = String.format(Locale.getDefault(), "%s.%d.Api%s(%s)",
                    UUID.randomUUID(),
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL);
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

    public static void logout() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clearLocalAuthenticationData();
                Context context = AppApplication.getAppContext();
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                sManager = null;
            }
        });
    }

    public static boolean isLogged() {
        return sManager != null;
    }

    public static void clearLocalAuthenticationData() {
        SharePreferenceManager.getIdentityPreferences().clear(false);
        DBManager.getInstance().getUserDao().cleanTable();
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

    private String mToken;
    private String mAESKey;
    private UserBean mUserBean;

    private IdentityManager(String token, String AESKey, UserBean userBean) {
        if (sManager != null) {
            throw new RuntimeException("Please use the 'getInstance' method to obtain the instance.");
        }
        mToken = token;
        mAESKey = AESKey;
        mUserBean = userBean;
    }

    public boolean updateUserInfo(UserBean user) {
        if (!user.isEmpty() && DBManager.getInstance().getUserDao().replace(user)) {
            mUserBean = user;
            return true;
        }
        return false;
    }

    public String getToken() {
        return mToken;
    }

    @Nullable
    public String getUserID() {
        return mUserBean.getUserID();
    }

    public UserBean getUser() {
        return mUserBean;
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


}
