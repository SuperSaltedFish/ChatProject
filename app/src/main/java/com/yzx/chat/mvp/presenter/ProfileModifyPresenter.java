package com.yzx.chat.mvp.presenter;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ProfileModifyEdit;
import com.yzx.chat.network.api.user.UploadAvatarBean;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.util.LogUtil;

import java.io.File;


/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProfileModifyPresenter implements ProfileModifyEdit.Presenter {
    private ProfileModifyEdit.View mProfileModifyView;

    @Override
    public void attachView(ProfileModifyEdit.View view) {
        mProfileModifyView = view;
    }

    @Override
    public void detachView() {
        mProfileModifyView = null;
    }

    @Override
    public UserBean getUserInfo() {
        return IMClient.getInstance().getUserManager().getUser();
    }

    @Override
    public void updateProfile(final UserBean user) {
        mProfileModifyView.setEnableProgressDialog(true);
        IMClient.getInstance().getUserManager().updateProfile(user.getNickname(), user.getSex(), user.getBirthday(), user.getLocation(), user.getSignature(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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
        IMClient.getInstance().getUserManager().uploadAvatar(avatarPath, new ResultCallback<UploadAvatarBean>() {
            @Override
            public void onSuccess(UploadAvatarBean result) {
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
