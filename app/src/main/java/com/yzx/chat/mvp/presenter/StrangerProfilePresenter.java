package com.yzx.chat.mvp.presenter;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.StrangerProfileContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AsyncResult;
import com.yzx.chat.util.AsyncUtil;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class StrangerProfilePresenter implements StrangerProfileContract.Presenter {

    private StrangerProfileContract.View mStrangerProfileView;
    private RequestContactResult mRequestContactResult;

    @Override
    public void attachView(StrangerProfileContract.View view) {
        mStrangerProfileView = view;

    }

    @Override
    public void detachView() {
        AsyncUtil.cancelResult(mRequestContactResult);
        mStrangerProfileView = null;
    }

    @Override
    public void requestContact(final UserBean user, final String verifyContent) {
        AsyncUtil.cancelResult(mRequestContactResult);
        mRequestContactResult = new RequestContactResult(this);
        IMClient.getInstance().contactManager().requestContact(user, verifyContent, mRequestContactResult);
    }

    private void requestComplete() {
        mStrangerProfileView.goBack();
    }

    private void requestFailure(String error) {
        mStrangerProfileView.showError(error);
    }

    private static class RequestContactResult extends AsyncResult<StrangerProfilePresenter, Boolean> {

        RequestContactResult(StrangerProfilePresenter dependent) {
            super(dependent);
        }

        @Override
        protected void onSuccessResult(StrangerProfilePresenter dependent, Boolean result) {
            dependent.requestComplete();
        }

        @Override
        protected void onFailureResult(StrangerProfilePresenter dependent, String error) {
            dependent.requestFailure(error);
        }
    }
}
