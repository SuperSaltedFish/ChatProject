package com.yzx.chat.module.login.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VerifyContract {
    public interface View extends BaseView<Presenter> {

        void showErrorHint(String error);

        void startSplashActivity();
    }


    public interface Presenter extends BasePresenter<View> {

        void login(String username, String password, String verifyCode,String serverSecretKey);

        void register(String username, String password, String nickname, String verifyCode,String serverSecretKey);

        void obtainLoginSMS(String username,String password,String serverSecretKey);

        void obtainRegisterSMS(String username,String serverSecretKey);

    }
}
