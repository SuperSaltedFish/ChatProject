package com.yzx.chat.module.login.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.module.login.contract.RegisterContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View mRegisterView;

    @Override
    public void attachView(RegisterContract.View view) {
        mRegisterView = view;
    }

    @Override
    public void detachView() {
        mRegisterView = null;
    }

    @Override
    public void obtainRegisterVerifyCode(String username) {
        AppClient.getInstance().obtainSMSOfRegisterType(username, new LifecycleMVPResultCallback<Void>(mRegisterView,false) {
            @Override
            protected void onSuccess(Void result) {
                mRegisterView.jumpToVerifyPage();
            }
        });
    }

}
