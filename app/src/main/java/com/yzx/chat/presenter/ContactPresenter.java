package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkAsyncTask;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactPresenter implements ContactContract.Presenter {

    private ContactContract.View mContactView;
    private RefreshAllContactsTask mRefreshContactsTask;
    private LoadUnreadCountTask mLoadUnreadCountTask;
    private List<ContactBean> mContactList;
    private Handler mHandler;
    private IMClient mIMClient;


    @Override
    public void attachView(ContactContract.View view) {
        mContactView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mContactList = new ArrayList<>(128);
        mIMClient.contactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.contactManager().addContactChangeListener(mOnContactChangeListener);

    }

    @Override
    public void detachView() {
        mIMClient.contactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.contactManager().removeContactChangeListener(mOnContactChangeListener);
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        NetworkUtil.cancelTask(mRefreshContactsTask);
        mContactView = null;
        mHandler.removeCallbacksAndMessages(null);
        mContactList.clear();
        mContactList = null;
        mHandler = null;
    }

    @Override
    public void loadUnreadCount() {
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        mLoadUnreadCountTask = new LoadUnreadCountTask();
        mLoadUnreadCountTask.execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContact() {
        LogUtil.e("loadAllContact");
        NetworkUtil.cancelTask(mRefreshContactsTask);
        mRefreshContactsTask = new RefreshAllContactsTask(this);
        mRefreshContactsTask.execute(mContactList);

    }


    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mContactView.updateContactListView(diffResult, mContactList);
    }

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContactView.updateUnreadBadge(count);
                }
            });
        }
    };

    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(final ContactBean contact) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadAllContact();
                }
            });
        }

        @Override
        public void onContactDeleted(final ContactBean contact) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadAllContact();
                }
            });
        }

        @Override
        public void onContactUpdate(final ContactBean contact) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int index = mContactList.indexOf(contact);
                    if (index >= 0) {
                        ContactBean old = mContactList.get(index);
                        if (!old.getName().equals(contact.getName())) {
                            loadAllContact();
                        }
                        mContactList.set(index, contact);
                        mContactView.updateContactItem(contact);
                    } else {
                        loadAllContact();
                    }
                }
            });
        }
    };


    private static class RefreshAllContactsTask extends NetworkAsyncTask<ContactPresenter, List<ContactBean>, DiffUtil.DiffResult> {

        RefreshAllContactsTask(ContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactBean>[] lists) {
            List<ContactBean> newList = IMClient.getInstance().contactManager().getAllContacts();
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
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }

    private static class LoadUnreadCountTask extends NetworkAsyncTask<Void, Void, Void> {

        LoadUnreadCountTask() {
            super(null);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

    }

}
