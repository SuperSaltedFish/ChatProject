package com.yzx.chat.core;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.UserDao;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.UserApi;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.Executor.HttpExecutor;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class UserManager {

    private UserEntity mUserEntity;
    private UserApi mUserApi;

    public UserManager(UserEntity userEntity) {
        if (userEntity == null || userEntity.isEmpty()) {
            throw new RuntimeException("token or user can't be empty");
        }
        mUserEntity = userEntity;
        mUserApi = ApiHelper.getProxyInstance(UserApi.class);
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

    public UserEntity getUser() {
        return UserEntity.copy(mUserEntity);
    }
}
