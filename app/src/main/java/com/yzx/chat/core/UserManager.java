package com.yzx.chat.core;


import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.UserDao;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.UserApi;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;


/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class UserManager {

    private AppClient mAppClient;
    private UserEntity mCurrentUser;
    private UserApi mUserApi;
    private UserDao mUserDao;
    private Map<String, UserEntity> mUserCacheMap;

    UserManager(AppClient appClient) {
        mAppClient = appClient;
        mUserApi = ApiHelper.getProxyInstance(UserApi.class);
        mUserCacheMap = Collections.synchronizedMap(new HashMap<String, UserEntity>(256));
    }

    void init(AbstractDao.ReadWriteHelper helper, UserEntity userEntity) {
        if (userEntity == null || userEntity.isEmpty()) {
            throw new RuntimeException("token or user can't be empty");
        }
        mCurrentUser = userEntity;
        mUserDao = new UserDao(helper);
        List<UserEntity> userList = mUserDao.loadAll();
        for (UserEntity user : userList) {
            mUserCacheMap.put(user.getUserID(), user);
        }
    }

    void destroy() {
        mCurrentUser = null;
        mUserDao = null;
    }


    public void updateProfile(final String nickname, final int sex, final String birthday, final String location, final String signature, final ResultCallback<Void> callback) {
        mUserApi.updateUserProfile(nickname, sex, birthday, location, signature)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        mCurrentUser.setNickname(nickname);
                        mCurrentUser.setSex(sex);
                        mCurrentUser.setBirthday(birthday);
                        mCurrentUser.setLocation(location);
                        mCurrentUser.setSignature(signature);
                        mUserCacheMap.put(mCurrentUser.getUserID(), mCurrentUser);
                        if (!mUserDao.update(mCurrentUser)) {
                            LogUtil.e("update userInfo fail");
                        }
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    public void uploadAvatar(String imagePath, final ResultCallback<String> callback) {
        mUserApi.uploadAvatar(imagePath, null)
                .enqueue(new ResponseHandler<>(new ResultCallback<UploadAvatarEntity>() {
                    @Override
                    public void onResult(UploadAvatarEntity result) {
                        mCurrentUser.setAvatar(result.getAvatarUrl());
                        mUserCacheMap.put(mCurrentUser.getUserID(), mCurrentUser);
                        if (!mUserDao.update(mCurrentUser)) {
                            LogUtil.e("update avatar fail");
                        }
                        CallbackUtil.callResult(result.getAvatarUrl(), callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    public void searchUser(String nicknameOrTelephone, final ResultCallback<List<UserEntity>> callback) {
        mUserApi.searchUser(nicknameOrTelephone)
                .enqueue(new ResponseHandler<>(new ResultCallback<ArrayList<UserEntity>>() {
                    @Override
                    public void onResult(ArrayList<UserEntity> result) {
                        if (result != null && result.size() != 0) {
                            for (UserEntity user : result) {
                                mUserCacheMap.put(user.getUserID(), user);
                            }
                            if (!mUserDao.replaceAll(result)) {
                                LogUtil.e("replaceAll userList fail");
                            }
                        }
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    public UserEntity findUserInfoFromLocal(String userID) {
        return mUserCacheMap.get(userID);
    }

    public void findUserInfoByID(String userID, final ResultCallback<UserEntity> callback) {
        mUserApi.getUserProfile(userID)
                .enqueue(new ResponseHandler<>(new ResultCallback<UserEntity>() {
                    @Override
                    public void onResult(UserEntity result) {
                        mUserCacheMap.put(result.getUserID(), result);
                        if (!mUserDao.replace(result)) {
                            LogUtil.e("replace userList fail");
                        }
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    @Nullable
    public String getUserID() {
        return mCurrentUser.getUserID();
    }

    public UserEntity getCurrentUser() {
        return UserEntity.copy(mCurrentUser);
    }

    static UserEntity getUserInfoFromDB(String userID, AbstractDao.ReadWriteHelper helper) {
        return new UserDao(helper).loadByKey(userID);
    }

    static boolean replaceUserInfoOnDB(UserEntity userInfo, AbstractDao.ReadWriteHelper helper) {
        return new UserDao(helper).replace(userInfo);
    }
}
