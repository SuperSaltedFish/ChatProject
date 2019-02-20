package com.yzx.chat.module.login.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.module.login.contract.LoginContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
public class LoginPresenter implements LoginContract.Presenter {

    private LoginContract.View mLoginView;

    @Override
    public void attachView(LoginContract.View view) {
        mLoginView = view;
    }

    @Override
    public void detachView() {
        mLoginView = null;
    }

    @Override
    public void tryLogin(final String username, String password) {
        AppClient.getInstance().login(username, password, null, new LifecycleMVPResultCallback<Void>(mLoginView, false) {
            @Override
            protected void onSuccess(Void result) {
                mLoginView.startHomeActivity();
            }

            @Override
            protected boolean onError(int code, String error) {
                if (code == ResponseHandler.ERROR_CODE_SERVER_SEND_LOGIN_VERIFY_CODE) {
                   AppClient.getInstance().obtainSMSOfLoginType(username, new LifecycleMVPResultCallback<Void>(mLoginView,false) {
                       @Override
                       protected void onSuccess(Void result) {
                           mLoginView.jumpToVerifyPage();
                       }
                   });
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
}
