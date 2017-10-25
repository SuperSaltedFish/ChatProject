package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年10月18日.
 * 其实你找不到错误不代表错误不存在，同样你看不到技术比你牛的人并不代表世界上没有技术比你牛的人。
 */

public class LoginContract {

    public interface View extends BaseView<Presenter> {
        void inputLoginVerifyCode(boolean isSkipVerify);

        void inputRegisterVerifyCode();

        void verifySuccess();

        void loginFailure(String reason);

        void registerFailure(String reason);

        void verifyFailure(String reason);
    }


    public interface Presenter extends BasePresenter<View> {
        void login(String username, String password, String verifyCode);

        void register(String username, String password, String nickname, String verifyCode);

        void loginVerify(String username, String password);

        void registerVerify(String username);
    }


}
