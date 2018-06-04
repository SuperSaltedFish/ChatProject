package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.mvp.contract.ContactProfileContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
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
        mIMClient.contactManager().removeContactChangeListener(mOnContactChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mContactProfileView = null;
    }


    @Override
    public void init(String contactID) {
        mContactBean = mIMClient.contactManager().getContact(contactID);
        if (mContactBean == null) {
            mContactProfileView.goBack();
        } else {
            mContactProfileView.updateContactInfo(mContactBean);
        }
    }

    @Override
    public ContactBean getContact() {
        return mContactBean;
    }

    @Override
    public void deleteContact() {
        mContactProfileView.enableProgressDialog(true);
        mIMClient.contactManager().deleteContact(mContactBean, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (mContactBean.getUserProfile().getUserID().equals(ChatPresenter.sConversationID)) {
                    mContactProfileView.finishChatActivity();
                }
                mContactProfileView.enableProgressDialog(false);
                mContactProfileView.goBack();
            }

            @Override
            public void onFailure(String error) {
                mContactProfileView.enableProgressDialog(false);
                mContactProfileView.showError(error);
            }
        });
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

}
