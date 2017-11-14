package com.yzx.chat.presenter;

import com.hyphenate.chat.EMClient;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class SplashPresenter implements SplashContract.Presenter {

    private SplashContract.View mSplashView;
    private InitAsyncTask mInitAsyncTask;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
    }

    @Override
    public void detachView() {
        NetworkUtil.cancel(mInitAsyncTask);
        mSplashView = null;
    }

    @Override
    public void init() {
        if (EMClient.getInstance().isLoggedInBefore() && IdentityManager.getInstance().isLogged()) {
            NetworkUtil.cancel(mInitAsyncTask);
            mInitAsyncTask = new InitAsyncTask(SplashPresenter.this);
            mInitAsyncTask.execute();
        } else {
            IdentityManager.getInstance().clearAuthenticationData();
            mSplashView.startLoginActivity();
        }
    }

    private void initSuccess() {
        mSplashView.startHomeActivity();
    }

    private void initFail(String error) {
        mSplashView.error(error);
    }

    private static class InitAsyncTask extends NetworkAsyncTask<Void, String> {

        InitAsyncTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Void... voids) {
            EMClient client = EMClient.getInstance();
            client.chatManager().loadAllConversations();
            client.groupManager().loadAllGroups();
            return null;
        }

        @Override
        protected void onPostExecute(String result, Object lifeCycleObject) {
            super.onPostExecute(result, lifeCycleObject);
            SplashPresenter presenter = (SplashPresenter) lifeCycleObject;
            if (result == null) {
                presenter.initSuccess();
            } else {
                presenter.initFail(result);
            }
        }
    }


}
