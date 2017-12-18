package com.yzx.chat.presenter;

import com.hyphenate.chat.EMClient;
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
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class SplashPresenter implements SplashContract.Presenter {
    //
    private SplashContract.View mSplashView;
    private InitAsyncTask mInitAsyncTask;
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
        NetworkUtil.cancelTask(mInitAsyncTask);
        NetworkUtil.cancelCall(mTokenVerify);
        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mSplashView = null;
    }

    @Override
    public void init() {
        if (IdentityManager.getInstance().isLogged() && EMClient.getInstance().isLoggedInBefore()) {
            initIMServer();
            initHTTPServer();
        } else {
            mSplashView.startLoginActivity();
        }

    }

    private void initIMServer() {
        NetworkUtil.cancelTask(mInitAsyncTask);
        mInitAsyncTask = new InitAsyncTask(SplashPresenter.this);
        mInitAsyncTask.execute();
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
                isSuccess = false;
                mSplashView.startLoginActivity();
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


    private void initIMServerSuccess() {
        if (mTaskCount.decrementAndGet() == 0) {
            mSplashView.startHomeActivity();
        }
    }

    private void initFail(String error) {
        mSplashView.error(error);
    }

    private static class InitAsyncTask extends NetworkAsyncTask<SplashPresenter, Void, String> {

        InitAsyncTask(SplashPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ChatClientManager.getInstance().loadAllConversationsAndGroups();
            return null;
        }

        @Override
        protected void onPostExecute(String result, SplashPresenter lifeDependentObject) {
            super.onPostExecute(result, lifeDependentObject);
            if (result == null) {
                lifeDependentObject.initIMServerSuccess();
            } else {
                lifeDependentObject.initFail(result);
            }
        }
    }


}
