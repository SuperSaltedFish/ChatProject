package com.yzx.chat.module.contact.presenter;


import android.text.TextUtils;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactProfileContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfilePresenter implements ContactProfileContract.Presenter {

    private ContactProfileContract.View mContactProfileView;
    private ContactEntity mContactEntity;
    private AppClient mAppClient;

    @Override
    public void attachView(ContactProfileContract.View view) {
        mContactProfileView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mAppClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mContactProfileView = null;
    }


    @Override
    public void init(String contactID) {
        mContactEntity = mAppClient.getContactManager().getContact(contactID);
        if (mContactEntity == null) {
            mContactProfileView.goBack();
            return;
        }
        mContactProfileView.updateContactInfo(mContactEntity);
    }

    @Override
    public ContactEntity getContact() {
        return mContactEntity;
    }

    @Override
    public void deleteContact() {
        mAppClient.getContactManager().deleteContact(mContactEntity.getUserInfo().getUserID(), new LifecycleMVPResultCallback<Void>(mContactProfileView) {
            @Override
            protected void onSuccess(Void result) {

            }

        });
    }

    @Override
    public ArrayList<String> getAllTags() {
        Set<String> tags = AppClient.getInstance().getContactManager().getAllTags();
        if (tags != null && tags.size() > 0) {
            return new ArrayList<>(tags);
        } else {
            return null;
        }
    }

    @Override
    public void saveRemarkInfo(ContactEntity contact) {
        AppClient.getInstance().getContactManager().updateContactRemark(contact.getContactID(), contact.getRemarkName(), contact.getDescription(), contact.getTelephones(), contact.getTags(), new LifecycleMVPResultCallback<Void>(mContactProfileView) {
            @Override
            protected void onSuccess(Void result) {

            }
        });
    }


    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactEntity contact) {

        }

        @Override
        public void onContactDeleted(String contactID) {
            if (TextUtils.equals(mContactEntity.getContactID(),contactID)) {
                mContactProfileView.goBack();
            }
        }

        @Override
        public void onContactUpdate(ContactEntity contact) {
            if (contact.equals(mContactEntity)) {
                mContactEntity = contact;
                mContactProfileView.updateContactInfo(mContactEntity);
            }
        }
    };

}
