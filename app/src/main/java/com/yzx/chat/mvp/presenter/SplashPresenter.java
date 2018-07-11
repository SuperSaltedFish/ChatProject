package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.mvp.contract.SplashContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.UserInfoBean;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.network.chat.UserManager;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.tool.SharePreferenceManager;
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
