package com.yzx.chat.module.login.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterContract {
    public interface View extends BaseView<Presenter> {
        void jumpToVerifyPage();

        void showErrorHint(String error);
    }


    public interface Presenter extends BasePresenter<View> {
        void obtainRegisterVerifyCode(String username);

        String getServerSecretKey();
    }
}
