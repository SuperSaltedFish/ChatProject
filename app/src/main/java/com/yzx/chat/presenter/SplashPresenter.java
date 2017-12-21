package com.yzx.chat.presenter;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.user.GetUserFriendsBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imlib.RongIMClient;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class SplashPresenter implements SplashContract.Presenter {
    //
    private SplashContract.View mSplashView;
    private Call<JsonResponse<Void>> mTokenVerify;
    private Call<JsonResponse<GetUserFriendsBean>> mGetUserFriendsTask;

    private AtomicInteger mTaskCount;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
        mTaskCount = new AtomicInteger(2);
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelCall(mTokenVerify);
        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mSplashView = null;
    }

    @Override
    public void init(boolean isAlreadyLoggedIM) {
        if (IdentityManager.getInstance().isLogged()) {
            if (isAlreadyLoggedIM) {
                mTaskCount.decrementAndGet();
            } else {
                initIMServer();
            }
            initHTTPServer();
        }else {
            mSplashView.startLoginActivity();
        }
    }

    private void initIMServer() {
        ChatClientManager.getInstance().login("nxv/AObbYd4yTGG14RkxiaE4ovwvabHEXU8xDrUJSvHwGIJoS4kz3vgMQ+4tQkG9HkDogLCSeC4Q1Tv4cVPPjmaWYJKFaTH8", new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                AndroidUtil.showToast(R.string.SplashPresenter_TokenIncorrect);
                mSplashView.startLoginActivity();
            }

            @Override
            public void onSuccess(String s) {
                if (mTaskCount.decrementAndGet() == 0) {
                    mSplashView.startHomeActivity();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
                AndroidUtil.showToast(R.string.SplashPresenter_LoginFailInIMSDK);
                mSplashView.startLoginActivity();
            }
        });
    }

    private void initHTTPServer() {
        UserApi userApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        AuthApi authApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);

        NetworkUtil.cancelCall(mTokenVerify);
        mTokenVerify = authApi.tokenVerify();
        mTokenVerify.setCallback(new BaseHttpCallback<Void>() {
            boolean isSuccess;

            @Override
            protected void onSuccess(Void response) {
                isSuccess = true;
            }

            @Override
            protected void onFailure(String message) {
                isSuccess = true;
                //mSplashView.startLoginActivity();
            }

            @Override
            public boolean isExecuteNextTask() {
                return isSuccess;
            }
        });

        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mGetUserFriendsTask = userApi.getUserFriends();
        mGetUserFriendsTask.setCallback(new BaseHttpCallback<GetUserFriendsBean>() {
            @Override
            protected void onSuccess(GetUserFriendsBean response) {
                DBManager.getInstance().getFriendDao().cleanTable();
                DBManager.getInstance().getFriendDao().insertAll(response.getFriends());
                if (mTaskCount.decrementAndGet() == 0) {
                    mSplashView.startHomeActivity();
                }
            }

            @Override
            protected void onFailure(String message) {
                LogUtil.e(message);
                if (mTaskCount.decrementAndGet() == 0) {
                    mSplashView.startHomeActivity();
                }
            }
        });

        sHttpExecutor.submit(mTokenVerify, mGetUserFriendsTask);
    }

}
