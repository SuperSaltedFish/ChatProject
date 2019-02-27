package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.contact.contract.ContactListContract;

import java.util.Set;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactListPresenter implements ContactListContract.Presenter {

    private ContactListContract.View mContactView;

    private AppClient mAppClient;

    @Override
    public void attachView(ContactListContract.View view) {
        mContactView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.getContactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
        mAppClient.getContactManager().addContactTagChangeListener(mOnContactTagChangeListener);
    }

    @Override
    public void detachView() {
        mAppClient.getContactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mAppClient.getContactManager().removeContactTagChangeListener(mOnContactTagChangeListener);
        mContactView = null;
    }

    @Override
    public void loadUnreadCount() {
        mContactView.updateUnreadBadge(mAppClient.getContactManager().getContactUnreadCount());
    }

    @Override
    public void loadAllContact() {
        LogUtil.e("loadAllContact");
        mContactView.showContactList(mAppClient.getContactManager().getAllContacts());

    }

    @Override
    public void loadTagCount() {
        Set<String> tags = mAppClient.getContactManager().getAllTags();
        mContactView.showTagCount(tags == null ? 0 : tags.size());
    }

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mContactView.updateUnreadBadge(count);
        }
    };

    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(final ContactEntity contact) {
            loadAllContact();
        }

        @Override
        public void onContactDeleted(final String contactID) {
            loadAllContact();
        }

        @Override
        public void onContactUpdate(final ContactEntity contact) {
            loadAllContact();
        }
    };

    private final ContactManager.OnContactTagChangeListener mOnContactTagChangeListener = new ContactManager.OnContactTagChangeListener() {
        @Override
        public void onContactTagAdded(Set<String> newTags) {
            loadTagCount();
        }

        @Override
        public void onContactTagDeleted(Set<String> deleteTags) {
            loadTagCount();
        }
    };


}
