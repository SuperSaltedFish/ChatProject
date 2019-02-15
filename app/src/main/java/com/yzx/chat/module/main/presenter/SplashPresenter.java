package com.yzx.chat.module.main.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.main.contract.SplashContract;
import com.yzx.chat.tool.SharePreferenceHelper;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;


/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class SplashPresenter implements SplashContract.Presenter {

    private SplashContract.View mSplashView;
    private AppClient mAppClient;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
        mAppClient = AppClient.getInstance();
    }

    @Override
    public void detachView() {
        mAppClient = null;
        mSplashView = null;
    }

    @Override
    public void checkLogin() {
        mAppClient.loginByLocalToken(new LifecycleMVPResultCallback<UserEntity>(mSplashView,false) {
            @Override
            protected void onSuccess(UserEntity result) {
                mSplashView.startHomeActivity();
            }

            @Override
            protected boolean onError(int code, String error) {
                if (SharePreferenceHelper.getConfigurePreferences().isFirstGuide()) {
                    mSplashView.startGuide();
                } else {
                    mSplashView.startLoginActivity();
                }
                return true;
            }
        });
    }

}
