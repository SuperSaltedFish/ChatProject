package com.yzx.chat.presenter;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class SplashPresenter implements SplashContract.Presenter {

    private SplashContract.View mSplashView;
    private InitChatAsyncTask mInitChatAsyncTask;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
    }

    @Override
    public void detachView() {
        if (mInitChatAsyncTask != null) {
            mInitChatAsyncTask.cancel();
        }
        mSplashView = null;
    }

    @Override
    public void initChat() {
        if (mInitChatAsyncTask != null) {
            mInitChatAsyncTask.cancel();
        }
        mInitChatAsyncTask = new InitChatAsyncTask(SplashPresenter.this);
        mInitChatAsyncTask.execute();
    }

    @Override
    public void initDatabase() {

    }

    private void complete() {
        mSplashView.complete();
    }

    private void error(String error) {
        mSplashView.error(error);
    }

    private static class InitChatAsyncTask extends NetworkAsyncTask<Void, String> {

        InitChatAsyncTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Void... voids) {
            EMClient client = EMClient.getInstance();
            if(client.isLoggedInBefore()) {
                client.chatManager().loadAllConversations();
                client.groupManager().loadAllGroups();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result, Object lifeCycleObject) {
            super.onPostExecute(result, lifeCycleObject);
            if (lifeCycleObject == null) {
                return;
            }
            SplashPresenter presenter = (SplashPresenter) lifeCycleObject;
            if (result == null) {
                presenter.complete();
            } else {
                presenter.error(result);
            }
        }
    }


}
