package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.FriendBean;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.LogUtil;
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
    private List<FriendBean> mFriendList;
    private Handler mHandler;

    @Override
    public void attachView(ContactContract.View view) {
        mContactView = view;
        mHandler = new Handler();
        mFriendList = new ArrayList<>(128);
        ChatClientManager.getInstance().addUnreadCountChangeListener(mUnreadCountChangeListener);
    }

    @Override
    public void detachView() {
        ChatClientManager.getInstance().removeUnreadCountChangeListener(mUnreadCountChangeListener);
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        NetworkUtil.cancelTask(mRefreshContactsTask);
        mContactView = null;
        mHandler.removeCallbacksAndMessages(null);
        mFriendList.clear();
        mFriendList = null;
        mHandler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllContact(List<FriendBean> oldData) {
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        mLoadUnreadCountTask = new LoadUnreadCountTask();
        mLoadUnreadCountTask.execute();

        NetworkUtil.cancelTask(mRefreshContactsTask);
        mRefreshContactsTask = new RefreshAllContactsTask(this);
        mRefreshContactsTask.execute(oldData, mFriendList);

    }

    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mContactView.updateContactListView(diffResult,mFriendList);
    }

    private final ChatClientManager.UnreadCountChangeListener mUnreadCountChangeListener = new ChatClientManager.UnreadCountChangeListener() {
        @Override
        public void onMessageUnreadCountChange(int unreadCount) {

        }

        @Override
        public void onContactUnreadCountChange(final int unreadCount) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContactView.updateUnreadBadge(unreadCount);
                }
            });
        }
    };

    private static class RefreshAllContactsTask extends NetworkAsyncTask<ContactPresenter,List<FriendBean>, DiffUtil.DiffResult> {

        RefreshAllContactsTask(ContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<FriendBean>[] lists) {
            lists[1].clear();
            DBManager.getInstance().getFriendDao().loadAllFriendTo(lists[1], IdentityManager.getInstance().getUserID());
            Collections.sort(lists[1], new Comparator<FriendBean>() {
                @Override
                public int compare(FriendBean o1, FriendBean o2) {
                    if (o2 != null && o1 != null) {
                        return o1.getAbbreviation().compareTo(o2.getAbbreviation());
                    } else {
                        return 0;
                    }
                }
            });

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<FriendBean>(lists[0], lists[1]) {
                @Override
                public boolean isItemEquals(FriendBean oldItem, FriendBean newItem) {
                    return oldItem.getFriendOf().equals(newItem.getFriendOf()) && oldItem.getUserID().equals(newItem.getUserID());
                }

                @Override
                public boolean isContentsEquals(FriendBean oldItem, FriendBean newItem) {
                    if (!oldItem.getAvatar().equals(newItem.getAvatar())) {
                        return false;
                    }
                    if (!oldItem.getRemarkName().equals(newItem.getRemarkName())) {
                        return false;
                    }
                    if (!oldItem.getNickname().equals(newItem.getNickname())) {
                        return false;
                    }
                    return true;
                }
            }, true);

            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }

    private static class LoadUnreadCountTask extends NetworkAsyncTask<Void,Void, Void> {

        LoadUnreadCountTask() {
            super(null);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ChatClientManager.getInstance().updateContactUnreadCount();
            return null;
        }

    }

}
