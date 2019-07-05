package com.yzx.chat.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yzx.chat.core.util.Base64Util;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.core.util.RSAUtil;

import java.nio.charset.Charset;
import java.security.KeyPair;

/**
 * Created by 叶智星 on 2018年09月18日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
@SuppressLint("ApplySharedPref")
public class StorageHelper {

    private static final String RSA_KET_ALIAS = StorageHelper.class.getName() + ".RSA";

    private SharedPreferences mConfigurationPreferences;
    private SharedPreferences mUserPreferences;
    private KeyPair mRSAKeyPair;

    //当AndroidKeyStore不能用的时候，就用下面临时的密钥
    private static final byte[] TEMP_1 = {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -77, 106, 50, -41, 22, -46, 98, -45, 127, 101, 29, -111, 30, -20, 24, -73, 36, -102, -120, 72, 76, -15, -63, 101, -22, -58, 70, 82, -84, -67, 107, 19, 90, 80, -56, -25, 85, -17, 29, -95, -40, 5, 24, 116, 20, -103, -125, -126, 19, 64, 26, -24, -22, 56, -37, -93, -31, 76, -79, -115, 84, -71, -117, 80, -53, 9, -81, 79, 99, -79, -49, -70, -16, 13, -122, -29, 125, -23, 125, 116, 90, -20, 111, 87, 40, 89, 9, 92, 91, -125, -82, -97, -24, -65, 93, 2, -73, -2, -115, 100, 4, 16, -52, -20, -86, -33, 61, 70, 30, -15, -46, -113, -87, 13, -12, 57, -84, 76, -46, -114, -41, -8, -71, -33, 41, 112, -14, 11, 2, 3, 1, 0, 1};
    private static final byte[] TEMP_2 = {48, -126, 2, 118, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 2, 96, 48, -126, 2, 92, 2, 1, 0, 2, -127, -127, 0, -77, 106, 50, -41, 22, -46, 98, -45, 127, 101, 29, -111, 30, -20, 24, -73, 36, -102, -120, 72, 76, -15, -63, 101, -22, -58, 70, 82, -84, -67, 107, 19, 90, 80, -56, -25, 85, -17, 29, -95, -40, 5, 24, 116, 20, -103, -125, -126, 19, 64, 26, -24, -22, 56, -37, -93, -31, 76, -79, -115, 84, -71, -117, 80, -53, 9, -81, 79, 99, -79, -49, -70, -16, 13, -122, -29, 125, -23, 125, 116, 90, -20, 111, 87, 40, 89, 9, 92, 91, -125, -82, -97, -24, -65, 93, 2, -73, -2, -115, 100, 4, 16, -52, -20, -86, -33, 61, 70, 30, -15, -46, -113, -87, 13, -12, 57, -84, 76, -46, -114, -41, -8, -71, -33, 41, 112, -14, 11, 2, 3, 1, 0, 1, 2, -127, -127, 0, -94, 82, 57, -81, 9, -47, -52, 97, -89, 71, -73, 105, 93, 125, -93, -102, -58, -66, 29, 0, -76, -60, -95, 1, -15, 87, -13, -75, -58, -35, 20, -20, 12, 113, 60, 98, -72, 7, -65, 90, 118, -45, -99, 93, 32, 106, 7, 101, -2, -66, -89, -38, 4, 86, -18, -72, -91, 20, -69, 42, -81, -11, -78, -119, 32, -15, 62, -123, 109, -35, 37, 42, 111, -51, 58, -87, 74, 59, 28, -2, -20, -15, 66, -50, -88, 99, -26, -50, -117, -26, -21, -84, -37, 117, 3, -102, 74, -78, -24, -84, 124, 96, -109, -73, 12, -26, 62, -126, -117, -108, -16, -7, -120, 108, 59, 16, 16, -112, 20, -63, 95, -51, -122, -3, -23, -100, -79, 57, 2, 65, 0, -31, 78, -47, -70, -111, -10, -28, 24, -107, 53, -27, -57, -43, 98, -34, -9, 50, -91, 48, -10, 97, 26, 125, -82, 87, 38, 99, -127, -111, -55, -57, 125, 82, 45, -128, 60, -79, -78, -86, 40, 64, 116, -68, 45, -58, 45, 71, 126, -3, -105, 65, 86, -29, -20, -58, -126, 86, 93, 2, 80, -64, 72, 46, 53, 2, 65, 0, -53, -38, -14, 123, -28, 118, 36, 82, 45, -95, 101, 10, 71, -39, -100, 34, -69, 35, 127, -12, 5, -37, -11, 65, 49, -43, -78, -45, -86, -49, -109, -108, -56, -67, 63, -32, -14, 18, -60, -74, 17, -10, 22, -80, -80, -39, 10, 94, -65, 25, -46, 113, 6, -105, 31, -29, -21, 66, 73, -27, -64, -123, -89, 63, 2, 64, 8, 40, 119, 117, -98, -11, 102, -102, -15, 68, 43, 86, -54, 6, 51, 118, -107, -15, -33, 76, -4, -123, 30, 9, -29, 3, -13, -44, 108, 55, -9, 46, 53, 124, -118, 73, 40, 82, 4, -47, 66, 76, 55, -56, 28, -123, -27, 49, -42, -14, -44, 29, -42, 80, -21, 123, 42, -81, 97, 119, 21, 15, 72, 5, 2, 64, 65, -115, -8, 26, 85, 62, -108, -79, -124, -112, -7, 32, 20, 56, -21, 87, -45, -36, 55, -94, 110, 91, -20, -125, 121, -106, 33, -6, -91, 92, 57, 54, 72, 83, 107, 126, 87, -6, 39, 29, -15, -86, -76, 99, 63, -117, -100, 37, 25, -55, 32, -2, 77, -61, 61, 115, 26, -51, 103, 92, 37, -52, 21, 27, 2, 64, 82, 79, 47, 104, -97, -101, -94, -96, -72, 87, -58, 72, 114, 10, 24, -31, 125, 8, 56, 60, -75, 24, 26, -116, -12, 99, -119, 87, -17, -62, -37, -39, 61, 62, 68, -75, 44, -41, 98, -91, 51, -5, -60, 59, 94, 122, 30, 26, -16, -10, 90, -67, 19, 65, -39, 14, -48, 57, 61, -66, 13, 119, -66, 65};

    StorageHelper(Context appContent, String storageName) {
        mConfigurationPreferences = appContent.getSharedPreferences(storageName, Context.MODE_PRIVATE);
        mRSAKeyPair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(appContent, RSA_KET_ALIAS);
        if (mRSAKeyPair == null) {
            LogUtil.w("generateRSAKeyPairInAndroidKeyStore fail");
            mRSAKeyPair = new KeyPair(RSAUtil.loadPublicKey(TEMP_1), RSAUtil.loadPrivateKey(TEMP_2));
        }
    }

    void initUserPreferences(Context context, String userID) {
        mUserPreferences = context.getSharedPreferences(userID, Context.MODE_PRIVATE);
    }

    void clearUserPreferences(Context context, String userID) {
        context.getSharedPreferences(userID, Context.MODE_PRIVATE).edit().clear().commit();
    }

    public boolean putToConfigurationPreferences(String key, String value) {
        if (!TextUtils.isEmpty(value) && mRSAKeyPair != null) {
            byte[] data = RSAUtil.encryptByPublicKey(value.getBytes(Charset.defaultCharset()), mRSAKeyPair.getPublic());
            if (data != null && data.length > 0) {
                value = Base64Util.encodeToString(data);
            } else {
                LogUtil.w("Encrypt fail,key=" + key);
                return false;
            }
        }
        return mConfigurationPreferences.edit().putString(key, value).commit();
    }

    public String getFromConfigurationPreferences(String key) {
        String value = mConfigurationPreferences.getString(key, null);
        if (!TextUtils.isEmpty(value) && mRSAKeyPair != null) {
            byte[] data = RSAUtil.decryptByPrivateKey(Base64Util.decode(value), mRSAKeyPair.getPrivate());
            if (data != null && data.length > 0) {
                return new String(data, Charset.defaultCharset());
            }
        }
        return null;
    }

    public boolean putToUserPreferences(String key, String value) {
        if (!TextUtils.isEmpty(value) && mRSAKeyPair != null) {
            byte[] data = RSAUtil.encryptByPublicKey(value.getBytes(Charset.defaultCharset()), mRSAKeyPair.getPublic());
            if (data != null && data.length > 0) {
                value = Base64Util.encodeToString(data);
            } else {
                LogUtil.w("Encrypt fail,key=" + key);
                return false;
            }
        }
        return mUserPreferences.edit().putString(key, value).commit();
    }

    public String getFromUserPreferences(String key) {
        if (mUserPreferences == null) {
            return null;
        }
        String value = mUserPreferences.getString(key, null);
        if (!TextUtils.isEmpty(value) && mRSAKeyPair != null) {
            byte[] data = RSAUtil.decryptByPrivateKey(Base64Util.decode(value), mRSAKeyPair.getPrivate());
            if (data != null && data.length > 0) {
                value = new String(data, Charset.defaultCharset());
            }
        }
        return value;
    }

    public void clear() {
        if (mUserPreferences != null) {
            mUserPreferences.edit().clear().commit();
        }
    }
}
