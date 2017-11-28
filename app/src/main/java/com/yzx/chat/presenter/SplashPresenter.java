package com.yzx.chat.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.GetUserFriendsBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
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
    private UserApi mUserApi;
    private InitAsyncTask mInitAsyncTask;
    private Call<JsonResponse<GetUserFriendsBean>> mGetUserFriendsTask;

    private AtomicInteger mAtomicInteger;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        mAtomicInteger = new AtomicInteger();
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelTask(mInitAsyncTask);
        mSplashView = null;
    }

    @Override
    public void init() {
        loadAllFriend();
//        mAtomicInteger.set(2);
////        if (EMClient.getInstance().isLoggedInBefore()) {
////            NetworkUtil.cancel(mInitAsyncTask);
////            mInitAsyncTask = new InitAsyncTask(SplashPresenter.this);
////            mInitAsyncTask.execute();
////        } else {
////            IdentityManager.getInstance().clearAuthenticationData();
////            mSplashView.startLoginActivity();
        if(EMClient.getInstance().isLoggedInBefore()){
            NetworkUtil.cancelTask(mInitAsyncTask);
            mInitAsyncTask = new InitAsyncTask(SplashPresenter.this);
            mInitAsyncTask.execute();
            loadAllFriend();
        }else {
            EMClient.getInstance().login("244546875", "12345678", new EMCallBack() {
                @Override
                public void onSuccess() {
                    NetworkUtil.cancelTask(mInitAsyncTask);
                    mInitAsyncTask = new InitAsyncTask(SplashPresenter.this);
                    mInitAsyncTask.execute();
                    loadAllFriend();
                }

                @Override
                public void onError(int code, String error) {
                    LogUtil.e(error);
                    mSplashView.startLoginActivity();
                }

                @Override
                public void onProgress(int progress, String status) {

                }
            });
        }
//        }


    }

    private void complete(){

    }

    private void loadAllFriend(){
        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mGetUserFriendsTask = mUserApi.getUserFriends();
        mGetUserFriendsTask.setCallback(new BaseHttpCallback<GetUserFriendsBean>() {
            @Override
            protected void onSuccess(GetUserFriendsBean response) {
                DBManager.getInstance().getFriendDao().replaceAll(response.getUserList());
            }

            @Override
            protected void onFailure(String message) {
                LogUtil.e(message);
            }
        });
        sHttpExecutor.submit(mGetUserFriendsTask);
    }


    private void initSuccess() {
        mSplashView.startHomeActivity();
    }

    private void initFail(String error) {
        mSplashView.error(error);
    }

    private static class InitAsyncTask extends NetworkAsyncTask<SplashPresenter,Void, String> {

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
                lifeDependentObject.initSuccess();
            } else {
                lifeDependentObject.initFail(result);
            }
        }
    }


}
