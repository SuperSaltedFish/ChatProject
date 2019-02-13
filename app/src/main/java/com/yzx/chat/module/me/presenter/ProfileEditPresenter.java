package com.yzx.chat.module.me.presenter;

import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.me.contract.ProfileEditContract;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.util.LogUtil;

import java.io.File;


/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProfileEditPresenter implements ProfileEditContract.Presenter {
    private ProfileEditContract.View mProfileModifyView;

    @Override
    public void attachView(ProfileEditContract.View view) {
        mProfileModifyView = view;
    }

    @Override
    public void detachView() {
        mProfileModifyView = null;
    }

    @Override
    public UserEntity getUserInfo() {
        return AppClient.getInstance().getUserManager().getUser();
    }

    @Override
    public void updateProfile(final UserEntity user) {
        mProfileModifyView.setEnableProgressDialog(true);
        AppClient.getInstance().getUserManager().updateProfile(user.getNickname(), user.getSex(), user.getBirthday(), user.getLocation(), user.getSignature(), new ResultCallback<Void>() {
            @Override
            public void onResult(Void result) {
                mProfileModifyView.setEnableProgressDialog(false);
                if (mProfileModifyView != null) {
                    mProfileModifyView.goBack();
                }

            }

            @Override
            public void onFailure(String error) {
                mProfileModifyView.setEnableProgressDialog(false);
                if (mProfileModifyView != null) {
                    mProfileModifyView.showError(error);
                }
            }
        });

    }

    @Override
    public void uploadAvatar(final String avatarPath) {
        mProfileModifyView.setEnableProgressDialog(true);
        AppClient.getInstance().getUserManager().uploadAvatar(avatarPath, new ResultCallback<UploadAvatarEntity>() {
            @Override
            public void onResult(UploadAvatarEntity result) {
                deleteFile(avatarPath);
                mProfileModifyView.setEnableProgressDialog(false);
                mProfileModifyView.showNewAvatar(result.getAvatarUrl());
            }

            @Override
            public void onFailure(final String error) {
                deleteFile(avatarPath);
                mProfileModifyView.setEnableProgressDialog(false);
                mProfileModifyView.showError(error);

            }
        });
    }

    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.delete()) {
            LogUtil.e("delete photo file fail.");
        }
    }
}
