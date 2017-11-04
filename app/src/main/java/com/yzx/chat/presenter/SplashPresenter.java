package com.yzx.chat.presenter;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.IdentityManager;
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
        if(mInitChatAsyncTask!=null){
            mInitChatAsyncTask.cancel();
        }
        mSplashView = null;
    }

    @Override
    public void initChat() {
        if (mInitChatAsyncTask != null) {
            mInitChatAsyncTask.cancel();
        }
        mInitChatAsyncTask = new InitChatAsyncTask(this);
        mInitChatAsyncTask.execute();
    }

    @Override
    public void initDatabase() {

    }

    private void complete() {
        mSplashView.complete();
    }

    private void error(){

    }

    private static class InitChatAsyncTask extends NetworkAsyncTask<Void, Void> {

        InitChatAsyncTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            IdentityManager manager = IdentityManager.getInstance();
            EMClient.getInstance().loginWithToken(manager.getUserID(), manager.getToken(), new EMCallBack() {
                @Override
                public void onSuccess() {
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SplashPresenter presenter = (SplashPresenter) getLifeCycleObject();
                            if (presenter != null) {
                                presenter.complete();
                            }
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    LogUtil.e("dwddawdwadwd");
                }

                @Override
                public void onProgress(int progress, String status) {

                }
            });
            return null;
        }
    }


}
