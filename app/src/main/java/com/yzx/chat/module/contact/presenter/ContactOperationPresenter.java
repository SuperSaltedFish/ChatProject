package com.yzx.chat.module.contact.presenter;


import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.module.contact.contract.ContactOperationContract;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationPresenter implements ContactOperationContract.Presenter {

    private ContactOperationContract.View mContactOperationContractView;
    private AppClient mAppClient;

    @Override
    public void attachView(ContactOperationContract.View view) {
        mContactOperationContractView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.getContactManager().addContactOperationListener(mOnContactOperationListener);
        NotificationHelper.getInstance().cancelAllContactOperationNotification();
    }

    @Override
    public void detachView() {
        mAppClient.getContactManager().removeContactOperationListener(mOnContactOperationListener);
        mContactOperationContractView = null;
        mAppClient = null;
    }

    @Override
    public void loadAllAndMakeAllAsRead() {
        mAppClient.getContactManager().makeAllContactOperationAsRead();
        mContactOperationContractView.showContactOperation(mAppClient.getContactManager().loadAllContactOperation());
    }


    @Override
    public void acceptContactRequest(final ContactOperationEntity contactOperation) {
        mAppClient.getContactManager().acceptContact(contactOperation.getUser().getUserID(), new LifecycleMVPResultCallback<Void>(mContactOperationContractView) {
            @Override
            protected void onSuccess(Void result) {

            }

        });
    }

    @Override
    public void refusedContactRequest(ContactOperationEntity contactOperation) {
        mAppClient.getContactManager().refusedContact(contactOperation.getUser().getUserID(), "", new LifecycleMVPResultCallback<Void>(mContactOperationContractView) {
            @Override
            protected void onSuccess(Void result) {

            }
        });
    }

    @Override
    public void removeContactOperation(ContactOperationEntity ContactOperation) {
        mAppClient.getContactManager().removeContactOperation(ContactOperation.getContactOperationID(), new LifecycleMVPResultCallback<Void>(mContactOperationContractView) {
            @Override
            protected void onSuccess(Void result) {

            }
        });
    }

    @Override
    public ContactEntity findContact(String userID) {
        return mAppClient.getContactManager().getContact(userID);
    }

    private final ContactManager.OnContactOperationListener mOnContactOperationListener = new ContactManager.OnContactOperationListener() {
        @Override
        public void onContactOperationReceive(final ContactOperationEntity message) {
            loadAllAndMakeAllAsRead();
        }

        @Override
        public void onContactOperationUpdate(final ContactOperationEntity message) {
            loadAllAndMakeAllAsRead();
        }

        @Override
        public void onContactOperationRemove(String contactOperationID) {
            mContactOperationContractView.showContactOperation(mAppClient.getContactManager().loadAllContactOperation());
        }
    };




}
