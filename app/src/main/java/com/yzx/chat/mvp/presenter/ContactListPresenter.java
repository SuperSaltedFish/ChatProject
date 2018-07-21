package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.mvp.contract.ContactListContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;
import com.yzx.chat.util.AsyncUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactListPresenter implements ContactListContract.Presenter {

    private ContactListContract.View mContactView;
    private RefreshAllContactsTask mRefreshContactsTask;
    private List<ContactBean> mContactList;

    private IMClient mIMClient;


    @Override
    public void attachView(ContactListContract.View view) {
        mContactView = view;
        mIMClient = IMClient.getInstance();
        mContactList = new ArrayList<>(128);
        mIMClient.getContactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.getContactManager().addContactChangeListener(mOnContactChangeListener);

    }

    @Override
    public void detachView() {
        mIMClient.getContactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        AsyncUtil.cancelTask(mRefreshContactsTask);
        mContactView = null;
        mContactList.clear();
        mContactList = null;
    }

    @Override
    public void loadUnreadCount() {
        mContactView.updateUnreadBadge(mIMClient.getContactManager().getContactUnreadCount());
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
        HashSet<String> tags = mIMClient.getContactManager().getAllTags();
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
        public void onContactAdded(final ContactBean contact) {
            loadAllContact();
        }

        @Override
        public void onContactDeleted(final ContactBean contact) {
            loadAllContact();
        }

        @Override
        public void onContactUpdate(final ContactBean contact) {
            int index = mContactList.indexOf(contact);
            if (index >= 0) {
                ContactBean old = mContactList.get(index);
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


    private static class RefreshAllContactsTask extends BackstageAsyncTask<ContactListPresenter, List<ContactBean>, DiffUtil.DiffResult> {

        RefreshAllContactsTask(ContactListPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactBean>[] lists) {
            List<ContactBean> newList = IMClient.getInstance().getContactManager().getAllContacts();
            List<ContactBean> oldList = lists[0];

            Collections.sort(newList, new Comparator<ContactBean>() {
                @Override
                public int compare(ContactBean o1, ContactBean o2) {
                    if (o2 != null && o1 != null) {
                        return o1.getAbbreviation().compareTo(o2.getAbbreviation());
                    } else {
                        return 0;
                    }
                }
            });

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactBean>(oldList, newList) {
                @Override
                public boolean isItemEquals(ContactBean oldItem, ContactBean newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean isContentsEquals(ContactBean oldItem, ContactBean newItem) {
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
