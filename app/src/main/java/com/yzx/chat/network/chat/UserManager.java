package com.yzx.chat.network.chat;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.UserDao;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UploadAvatarBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class UserManager {

    private String mToken;
    private UserBean mUserBean;
    private UserApi mUserApi;
    private NetworkExecutor mNetworkExecutor;
    private Call<JsonResponse<Void>> mUpdateUserProfileCall;
    private Call<JsonResponse<UploadAvatarBean>> mUploadAvatarCall;

    private UserManager(String token, UserBean userBean) {
        if (TextUtils.isEmpty(token) || userBean == null || userBean.isEmpty()) {
            throw new RuntimeException("token or user can't be empty");
        }
        mToken = token;
        mUserBean = userBean;
        mUserApi = (UserApi) ApiHelper.getProxyInstance(UserApi.class);
        mNetworkExecutor = NetworkExecutor.getInstance();
    }

    void destroy() {
        AsyncUtil.cancelCall(mUpdateUserProfileCall);
        AsyncUtil.cancelCall(mUploadAvatarCall);
    }

    public void updateProfile(final String nickname, final int sex, final String birthday, final String location, final String signature, final ResultCallback<Void> callback) {
        AsyncUtil.cancelCall(mUpdateUserProfileCall);
        mUpdateUserProfileCall = mUserApi.updateUserProfile(nickname, sex, birthday, location, signature);
        mUpdateUserProfileCall.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                mUserBean.setNickname(nickname);
                mUserBean.setSex(sex);
                mUserBean.setBirthday(birthday);
                mUserBean.setLocation(location);
                mUserBean.setSignature(signature);
                callback.onSuccess(response);
            }

            @Override
            protected void onFailure(String message) {
                callback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mUpdateUserProfileCall);
    }

    public void uploadAvatar(String imagePath, final ResultCallback<UploadAvatarBean> callback) {
        AsyncUtil.cancelCall(mUploadAvatarCall);
        mUploadAvatarCall = mUserApi.uploadAvatar(imagePath);
        mUploadAvatarCall.setCallback(new BaseHttpCallback<UploadAvatarBean>() {
            @Override
            protected void onSuccess(UploadAvatarBean response) {
                mUserBean.setAvatar(response.getAvatarUrl());
                callback.onSuccess(response);
            }

            @Override
            protected void onFailure(String message) {
                callback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mUploadAvatarCall);
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


    public synchronized static boolean update(String token, UserBean user, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (TextUtils.isEmpty(token) || user == null || user.isEmpty()) {
            return false;
        }
        KeyPair rsaKeyPair = CryptoManager.getRSAKeyPair();
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();

        byte enToken[] = RSAUtil.encryptByPublicKey(token.getBytes(), rsaKeyPair.getPublic());
        if (enToken == null || enToken.length == 0) {
            return false;
        }
        String tmpToken = Base64Util.encodeToString(enToken);
        if (TextUtils.isEmpty(tmpToken)) {
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

        if (!idPref.putToken(tmpToken) || !idPref.putUserID(tmpUserID)) {
            idPref.clear(false);
            return false;
        }
        if (!new UserDao(readWriteHelper).replace(user)) {
            return false;
        }
        return true;
    }

    synchronized static UserManager getInstanceFromLocal(AbstractDao.ReadWriteHelper readWriteHelper) {
        String token = getLocalToken();
        String userID = getLocalUserID();
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(userID)) {
            return null;
        }
        UserBean user = new UserDao(readWriteHelper).loadByKey(userID);
        if (user == null || user.isEmpty()) {
            return null;
        }
        return new UserManager(token, user);
    }

    static String getLocalUserID() {
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        KeyPair rsaKeyPair = CryptoManager.getRSAKeyPair();
        String strUserID = idPref.getUserID();
        byte userID[] = Base64Util.decode(strUserID);
        if (userID == null || userID.length == 0) {
            return null;
        }
        userID = RSAUtil.decryptByPrivateKey(userID, rsaKeyPair.getPrivate());
        if (userID == null || userID.length == 0) {
            return null;
        }
        return new String(userID);
    }

    public static String getLocalToken() {
        SharePreferenceManager.IdentityPreferences idPref = SharePreferenceManager.getIdentityPreferences();
        String strToken = idPref.getToken();
        KeyPair rsaKeyPair = CryptoManager.getRSAKeyPair();
        byte token[] = Base64Util.decode(strToken);
        if (token == null || token.length == 0) {
            return null;
        }
        token = RSAUtil.decryptByPrivateKey(token, rsaKeyPair.getPrivate());
        if (token == null || token.length == 0) {
            return null;
        }
        return new String(token);
    }


}
