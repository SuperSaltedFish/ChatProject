package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.contact.contract.StrangerProfileContract;
import com.yzx.chat.core.IMClient;
import com.yzx.chat.core.listener.ResultCallback;

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
    public void requestContact(final UserEntity user, final String verifyContent) {
        IMClient.getInstance().getContactManager().requestContact(user, verifyContent, mAcceptOrRequestCallback);
    }

    @Override
    public void acceptContactRequest(ContactOperationEntity contactOperation) {
        mStrangerProfileView.setEnableProgressDialog(true);
        IMClient.getInstance().getContactManager().acceptContact(contactOperation, mAcceptOrRequestCallback);
    }

    private final ResultCallback<Void> mAcceptOrRequestCallback = new ResultCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            mStrangerProfileView.setEnableProgressDialog(false);
            mStrangerProfileView.goBack();
        }

        @Override
        public void onFailure(String error) {
            mStrangerProfileView.showError(error);
            mStrangerProfileView.setEnableProgressDialog(false);
        }
    };

}
