package com.yzx.chat.module.me.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.me.contract.ProfileEditContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

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
        return AppClient.getInstance().getUserManager().getCurrentUser();
    }

    @Override
    public void updateProfile(final UserEntity user) {
        AppClient.getInstance().getUserManager().updateProfile(user.getNickname(), user.getSex(), user.getBirthday(), user.getLocation(), user.getSignature(), new LifecycleMVPResultCallback<Void>(mProfileModifyView) {
            @Override
            protected void onSuccess(Void result) {
                mProfileModifyView.goBack();
            }
        });

    }

    @Override
    public void uploadAvatar(final String avatarPath) {
        AppClient.getInstance().getUserManager().uploadAvatar(avatarPath, new LifecycleMVPResultCallback<String>(mProfileModifyView) {
            @Override
            protected void onSuccess(String result) {
                deleteFile(avatarPath);
                mProfileModifyView.showNewAvatar(result);
            }

            @Override
            protected boolean onError(int code, String error) {
                deleteFile(avatarPath);
                return super.onError(code, error);
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
