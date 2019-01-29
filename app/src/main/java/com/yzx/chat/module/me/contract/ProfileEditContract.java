package com.yzx.chat.module.me.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.UserEntity;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProfileEditContract {

    public interface View extends BaseView<Presenter> {
        void showNewAvatar(String avatarPath);

        void setEnableProgressDialog(boolean isEnable);

        void showError(String error);

        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {
        UserEntity getUserInfo();

        void uploadAvatar(String avatarPath);

        void updateProfile(UserEntity user);
    }
}
