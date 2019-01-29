package com.yzx.chat.module.contact.presenter;


import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.module.contact.contract.ContactProfileContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfilePresenter implements ContactProfileContract.Presenter {

    private ContactProfileContract.View mContactProfileView;
    private ContactBean mContactBean;
    private IMClient mIMClient;

    @Override
    public void attachView(ContactProfileContract.View view) {
        mContactProfileView = view;
        mIMClient = IMClient.getInstance();
        mIMClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mContactProfileView = null;
    }


    @Override
    public void init(String contactID) {
        mContactBean = mIMClient.getContactManager().getContact(contactID);
        if (mContactBean == null) {
            mContactProfileView.goBack();
            return;
        }
        mContactProfileView.updateContactInfo(mContactBean);
    }

    @Override
    public ContactBean getContact() {
        return mContactBean;
    }

    @Override
    public void deleteContact() {
        mContactProfileView.setEnableProgressDialog(true);
        mIMClient.getContactManager().deleteContact(mContactBean, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {

            }

            @Override
            public void onFailure(String error) {
                mContactProfileView.setEnableProgressDialog(false);
                mContactProfileView.showError(error);
            }
        });
    }

    @Override
    public ArrayList<String> getAllTags() {
        Set<String> tags = IMClient.getInstance().getContactManager().getAllTags();
        if (tags != null && tags.size() > 0) {
            return new ArrayList<>(tags);
        } else {
            return null;
        }
    }

    @Override
    public void saveRemarkInfo(ContactBean contact) {
        IMClient.getInstance().getContactManager().updateContactRemark(contact, null);
    }


    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactBean contact) {

        }

        @Override
        public void onContactDeleted(ContactBean contact) {
            if (contact.equals(mContactBean)) {
                mContactProfileView.setEnableProgressDialog(false);
                mContactProfileView.goBack();
            }
        }

        @Override
        public void onContactUpdate(ContactBean contact) {
            if (contact.equals(mContactBean)) {
                mContactBean = contact;
                mContactProfileView.updateContactInfo(mContactBean);
            }
        }
    };

}
