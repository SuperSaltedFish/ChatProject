package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.contact.contract.StrangerProfileContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

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
        AppClient.getInstance().getContactManager().requestContact(user.getUserID(), verifyContent, new LifecycleMVPResultCallback<Void>(mStrangerProfileView) {
            @Override
            protected void onSuccess(Void result) {
                mStrangerProfileView.goBack();
            }
        });
    }

    @Override
    public void acceptContactRequest(ContactOperationEntity contactOperation) {
        AppClient.getInstance().getContactManager().acceptContact(contactOperation.getUserInfo().getUserID(), new LifecycleMVPResultCallback<ContactEntity>(mStrangerProfileView) {
            @Override
            protected void onSuccess(ContactEntity result) {
                mStrangerProfileView.goBack();
            }
        });
    }

}
