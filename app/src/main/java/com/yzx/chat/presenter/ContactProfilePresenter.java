package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactProfileContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactProfilePresenter implements ContactProfileContract.Presenter {

    private ContactProfileContract.View mContactProfileView;
    private ContactBean mContactBean;
    private Handler mHandler;

    @Override
    public void attachView(ContactProfileContract.View view) {
        mContactProfileView = view;
        mHandler = new Handler();
        IMClient.getInstance().contactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        IMClient.getInstance().contactManager().removeContactChangeListener(mOnContactChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mContactProfileView = null;
    }

    @Override
    public void init(ContactBean contact) {
        mContactBean = contact;
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
