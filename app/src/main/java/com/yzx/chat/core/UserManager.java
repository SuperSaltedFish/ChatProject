package com.yzx.chat.core;


import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.UserDao;
import com.yzx.chat.core.entity.GetUserProfileEntity;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.UserApi;
import com.yzx.chat.core.util.CallbackUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class UserManager {

    private AppClient mAppClient;
    private UserEntity mUserEntity;
    private UserApi mUserApi;
    private UserDao mUserDao;

    UserManager(AppClient appClient ) {
        mAppClient = appClient;
        mUserApi = ApiHelper.getProxyInstance(UserApi.class);
    }

    void init(AbstractDao.ReadWriteHelper helper, UserEntity userEntity) {
        if (userEntity == null || userEntity.isEmpty()) {
            throw new RuntimeException("token or user can't be empty");
        }
        mUserEntity = userEntity;
        mUserDao = new UserDao(helper);
    }

    void destroy() {
        mUserEntity = null;
        mUserDao = null;
    }


    public void updateProfile(final String nickname, final int sex, final String birthday, final String location, final String signature, final ResultCallback<Void> callback) {
        mUserApi.updateUserProfile(nickname, sex, birthday, location, signature)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        mUserEntity.setNickname(nickname);
                        mUserEntity.setSex(sex);
                        mUserEntity.setBirthday(birthday);
                        mUserEntity.setLocation(location);
                        mUserEntity.setSignature(signature);
                        mUserDao.update(mUserEntity);
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void uploadAvatar(String imagePath, final ResultCallback<UploadAvatarEntity> callback) {
        mUserApi.uploadAvatar(imagePath, null)
                .enqueue(new ResponseHandler<>(new ResultCallback<UploadAvatarEntity>() {
                    @Override
                    public void onResult(UploadAvatarEntity result) {
                        mUserEntity.setAvatar(result.getAvatarUrl());
                        mUserDao.update(mUserEntity);
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void searchUser(String nicknameOrTelephone, final ResultCallback<List<UserEntity>> callback) {
        mUserApi.searchUser(nicknameOrTelephone)
                .enqueue(new ResponseHandler<>(new ResultCallback<ArrayList<UserEntity>>() {
                    @Override
                    public void onResult(ArrayList<UserEntity> result) {
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void findUserProfileByID(String userID, final ResultCallback<GetUserProfileEntity> callback) {
        mUserApi.getUserProfile(userID)
                .enqueue(new ResponseHandler<>(new ResultCallback<GetUserProfileEntity>() {
                    @Override
                    public void onResult(GetUserProfileEntity result) {
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    @Nullable
    public String getUserID() {
        return mUserEntity.getUserID();
    }

    public UserEntity getCurrentUser() {
        return UserEntity.copy(mUserEntity);
    }

    static UserEntity getUserInfoFromDB(String userID, AbstractDao.ReadWriteHelper helper) {
        return new UserDao(helper).loadByKey(userID);
    }

    static boolean replaceUserInfoOnDB(UserEntity userInfo, AbstractDao.ReadWriteHelper helper) {
        return new UserDao(helper).replace(userInfo);
    }
}
