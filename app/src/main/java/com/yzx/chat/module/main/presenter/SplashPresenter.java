package com.yzx.chat.module.main.presenter;

import android.text.TextUtils;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.module.main.contract.SplashContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;


/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class SplashPresenter implements SplashContract.Presenter {

    public static final String KEY_NON_FIRST_START = "IsNonFirstStart";

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
        if (mAppClient.isLogged()) {
            mSplashView.startHomeActivity();
        } else {
            mAppClient.loginByLocalToken(new LifecycleMVPResultCallback<Void>(mSplashView, false) {
                @Override
                protected void onSuccess(Void result) {
                    mSplashView.startHomeActivity();
                }

                @Override
                protected boolean onError(int code, String error) {
                    if (code != ResponseHandler.ERROR_CODE_NOT_LOGGED_IN) {
                        mSplashView.showLoginError(error);
                    } else {
                        if (TextUtils.equals(mAppClient.getStorageHelper().getFromConfigurationPreferences(KEY_NON_FIRST_START), String.valueOf(true))) {
                            mSplashView.startLoginActivity();
                        } else {
                            mSplashView.startGuideActivity();
                        }
                    }
                    return true;
                }
            });
        }
    }

}
