package com.yzx.chat.module.login.presenter;


import com.yzx.chat.core.AppClient;
import com.yzx.chat.module.login.contract.VerifyContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VerifyPresenter implements VerifyContract.Presenter {

    private VerifyContract.View mVerifyView;

    @Override
    public void attachView(VerifyContract.View view) {
        mVerifyView = view;
    }

    @Override
    public void detachView() {
        mVerifyView = null;
    }


    @Override
    public void login(String username, String password, String verifyCode) {
        AppClient.getInstance().login(username, password, verifyCode, new LifecycleMVPResultCallback<Void>(mVerifyView) {
            @Override
            protected void onSuccess(Void result) {
                mVerifyView.startHomeActivity();
            }
        });
    }

    @Override
    public void register(String username, String password, String nickname, String verifyCode) {
        AppClient.getInstance().register(username, password, nickname, verifyCode, new LifecycleMVPResultCallback<Void>(mVerifyView) {
            @Override
            protected void onSuccess(Void result) {
                mVerifyView.goBack();
            }
        });
    }

    @Override
    public void obtainLoginSMS(String username, String password) {
        AppClient.getInstance().obtainSMSOfLoginType(username, new LifecycleMVPResultCallback<Void>(mVerifyView) {
            @Override
            protected void onSuccess(Void result) {
                mVerifyView.showCountDown();
            }
        });
    }

    @Override
    public void obtainRegisterSMS(String username) {
        AppClient.getInstance().obtainSMSOfRegisterType(username, new LifecycleMVPResultCallback<Void>(mVerifyView) {
            @Override
            protected void onSuccess(Void result) {
                mVerifyView.showCountDown();
            }
        });
    }

}
