package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactProfileContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AsyncResult;
import com.yzx.chat.util.AsyncUtil;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfilePresenter implements ContactProfileContract.Presenter {

    private ContactProfileContract.View mContactProfileView;
    private ContactBean mContactBean;
    private Handler mHandler;
    private DeleteContactResult mDeleteContactResult;
    private IMClient mIMClient;

    @Override
    public void attachView(ContactProfileContract.View view) {
        mContactProfileView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelResult(mDeleteContactResult);
        mIMClient.contactManager().removeContactChangeListener(mOnContactChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mContactProfileView = null;
    }


    @Override
    public void init(String contactID) {
        mContactBean = mIMClient.contactManager().getContact(contactID);
        if (mContactBean == null) {
            mContactProfileView.goBack();
        }else {
            mContactProfileView.updateContactInfo(mContactBean);
        }
    }

    @Override
    public ContactBean getContact() {
        return mContactBean;
    }

    @Override
    public void deleteContact() {
        AsyncUtil.cancelResult(mDeleteContactResult);
        mDeleteContactResult = new DeleteContactResult(this);
        mIMClient.contactManager().deleteContact(mContactBean, mDeleteContactResult);
    }

    void deleteContactSuccess() {
        mContactProfileView.goBack();
    }

    void deleteContactFailure(String error) {
        mContactProfileView.showError(error);
    }


    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactBean contact) {

        }

        @Override
        public void onContactDeleted(ContactBean contact) {

        }

        @Override
        public void onContactUpdate(final ContactBean contact) {
            if (contact.equals(mContactBean)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContactProfileView.updateContactInfo(contact);
                    }
                });
            }
        }
    };

    private static class DeleteContactResult extends AsyncResult<ContactProfilePresenter, Boolean> {


        DeleteContactResult(ContactProfilePresenter dependent) {
            super(dependent);
        }

        @Override
        protected void onSuccessResult(ContactProfilePresenter dependent, Boolean result) {
            dependent.deleteContactSuccess();
        }

        @Override
        protected void onFailureResult(ContactProfilePresenter dependent, String error) {
            dependent.deleteContactFailure(error);
        }
    }
}
