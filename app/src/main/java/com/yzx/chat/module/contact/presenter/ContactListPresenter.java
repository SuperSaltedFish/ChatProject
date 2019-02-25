package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactListContract;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;
import com.yzx.chat.util.AsyncUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.DiffUtil;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactListPresenter implements ContactListContract.Presenter {

    private ContactListContract.View mContactView;
    private RefreshAllContactsTask mRefreshContactsTask;
    private List<ContactEntity> mContactList;

    private AppClient mAppClient;


    @Override
    public void attachView(ContactListContract.View view) {
        mContactView = view;
        mAppClient = AppClient.getInstance();
        mContactList = new ArrayList<>(128);
        mAppClient.getContactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
        mAppClient.getContactManager().addContactTagChangeListener(mOnContactTagChangeListener);
    }

    @Override
    public void detachView() {
        mAppClient.getContactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mAppClient.getContactManager().removeContactTagChangeListener(mOnContactTagChangeListener);
        AsyncUtil.cancelTask(mRefreshContactsTask);
        mContactView = null;
        mContactList.clear();
        mContactList = null;
    }

    @Override
    public void loadUnreadCount() {
        mContactView.updateUnreadBadge(mAppClient.getContactManager().getContactUnreadCount());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContact() {
        LogUtil.e("loadAllContact");
        AsyncUtil.cancelTask(mRefreshContactsTask);
        mRefreshContactsTask = new RefreshAllContactsTask(this);
        mRefreshContactsTask.execute(mContactList);

    }

    @Override
    public void loadTagCount() {
        Set<String> tags = mAppClient.getContactManager().getAllTags();
        mContactView.showTagCount(tags == null ? 0 : tags.size());
    }


    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mContactView.updateContactListView(diffResult, mContactList);
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
            int index = mContactList.indexOf(contact);
            if (index >= 0) {
                ContactEntity old = mContactList.get(index);
                if (!old.getName().equals(contact.getName())) {
                    loadAllContact();
                } else {
                    mContactList.set(index, contact);
                    mContactView.updateContactItem(contact);
                }
            } else {
                loadAllContact();
            }
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

    private static class RefreshAllContactsTask extends BackstageAsyncTask<ContactListPresenter, List<ContactEntity>, DiffUtil.DiffResult> {

        RefreshAllContactsTask(ContactListPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactEntity>[] lists) {
            List<ContactEntity> newList = AppClient.getInstance().getContactManager().getAllContacts();
            List<ContactEntity> oldList = lists[0];

            Collections.sort(newList, new Comparator<ContactEntity>() {
                @Override
                public int compare(ContactEntity o1, ContactEntity o2) {
                    if (o2 != null && o1 != null) {
                        return o1.getAbbreviation().compareTo(o2.getAbbreviation());
                    } else {
                        return 0;
                    }
                }
            });

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactEntity>(oldList, newList) {
                @Override
                public boolean isItemEquals(ContactEntity oldItem, ContactEntity newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean isContentsEquals(ContactEntity oldItem, ContactEntity newItem) {
                    if (!oldItem.getName().equals(newItem.getName())) {
                        return false;
                    }
                    if (!oldItem.getUserProfile().getAvatar().equals(newItem.getUserProfile().getAvatar())) {
                        return false;
                    }
                    if (!oldItem.getUserProfile().getNickname().equals(newItem.getUserProfile().getNickname())) {
                        return false;
                    }
                    return true;
                }
            }, true);

            oldList.clear();
            oldList.addAll(newList);
            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactListPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }


}
