package com.yzx.chat.module.main.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.module.main.contract.SplashContract;
import com.yzx.chat.core.net.api.JsonResponse;
import com.yzx.chat.core.net.api.auth.AuthApi;
import com.yzx.chat.core.net.api.auth.UserInfoBean;
import com.yzx.chat.core.IMClient;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.api.ApiHelper;
import com.yzx.chat.core.manager.UserManager;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.core.manager.SharePreferenceManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.AsyncUtil;


/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class SplashPresenter implements SplashContract.Presenter {
    //
    private SplashContract.View mSplashView;
    private Call<JsonResponse<UserInfoBean>> mTokenVerify;


    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mTokenVerify);
        mSplashView = null;
    }

    @Override
    public void checkLogin() {
        if (IMClient.getInstance().isLogged()) {
            initDirectory();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSplashView.startHomeActivity();
                }
            }, 1000);
        } else {
            String token = UserManager.getLocalToken();
            if (TextUtils.isEmpty(token)) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (SharePreferenceManager.getConfigurePreferences().isFirstGuide()) {
                            mSplashView.startGuide();
                        } else {
                            mSplashView.startLoginActivity();
                        }
                    }
                }, 1000);

            } else {
                mTokenVerify = ((AuthApi) ApiHelper.getProxyInstance(AuthApi.class)).tokenVerify();
                IMClient.getInstance().loginByToken(mTokenVerify, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                initDirectory();
                                mSplashView.startHomeActivity();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        LogUtil.e(error);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mSplashView.showError(AndroidUtil.getString(R.string.SplashPresenter_TokenIncorrect));
                                mSplashView.startLoginActivity();
                            }
                        });
                    }
                });
            }
        }
    }


    private void initDirectory() {
        DirectoryHelper.initUserDirectory(IMClient.getInstance().getUserManager().getUserID());
    }
}
