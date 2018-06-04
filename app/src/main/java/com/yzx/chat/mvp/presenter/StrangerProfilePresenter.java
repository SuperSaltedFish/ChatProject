package com.yzx.chat.mvp.presenter;

import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.StrangerProfileContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.util.AsyncResult;
import com.yzx.chat.util.AsyncUtil;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class StrangerProfilePresenter implements StrangerProfileContract.Presenter {

    private StrangerProfileContract.View mStrangerProfileView;

    @Override
    public void attachView(StrangerProfileContract.View view) {
        mStrangerProfileView = view;

    }

    @Override
    public void detachView() {
        mStrangerProfileView = null;
    }

    @Override
    public void requestContact(final UserBean user, final String verifyContent) {
        IMClient.getInstance().contactManager().requestContact(user, verifyContent, mAcceptOrRequestCallback);
    }

    @Override
    public void acceptContactRequest(ContactOperationBean contactOperation) {
        mStrangerProfileView.enableProgressDialog(true);
        IMClient.getInstance().contactManager().acceptContact(contactOperation, mAcceptOrRequestCallback);
    }

    private final ResultCallback mAcceptOrRequestCallback = new ResultCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
            mStrangerProfileView.enableProgressDialog(false);
            mStrangerProfileView.goBack();
        }

        @Override
        public void onFailure(String error) {
            mStrangerProfileView.showError(error);
            mStrangerProfileView.enableProgressDialog(false);
        }
    };

}
