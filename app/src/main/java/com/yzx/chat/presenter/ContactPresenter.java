package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.bean.FriendBean;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
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
        mLoadUnreadCountTask = new LoadUnreadCountTask(this);
        mLoadUnreadCountTask.execute();

        NetworkUtil.cancelTask(mRefreshContactsTask);
        mRefreshContactsTask = new RefreshAllContactsTask(this);
        mRefreshContactsTask.execute(oldData, mFriendList);
    }

    @Override
    public void loadUnreadComplete(int count) {
        mContactView.updateUnreadBadge(count);
        ChatClientManager.getInstance().setContactUnreadCount(count);
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
            return DiffUtil.calculateDiff(new DiffCallback(lists[0], lists[1]), true);
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }

    private static class LoadUnreadCountTask extends NetworkAsyncTask<ContactPresenter,Void, Integer> {

        LoadUnreadCountTask(ContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int count = DBManager.getInstance().getContactDao().loadRemindCount();
            ChatClientManager.getInstance().setContactUnreadCount(count);
            return count;
        }

        @Override
        protected void onPostExecute(Integer integer, ContactPresenter lifeDependentObject) {
            super.onPostExecute(integer, lifeDependentObject);
            lifeDependentObject.loadUnreadComplete(integer);
        }
    }


    private static class DiffCallback extends DiffUtil.Callback {

        private List<FriendBean> mNewData;
        private List<FriendBean> mOldData;

        DiffCallback(List<FriendBean> oldData, List<FriendBean> newData) {
            this.mOldData = oldData;
            this.mNewData = newData;
        }

        @Override
        public int getOldListSize() {
            return mOldData == null ? 0 : mOldData.size();
        }

        @Override
        public int getNewListSize() {
            return mNewData == null ? 0 : mNewData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            FriendBean oldBean = mOldData.get(oldItemPosition);
            FriendBean newBean = mNewData.get(oldItemPosition);
            return oldBean.getFriendOf().equals(newBean.getFriendOf()) && oldBean.getUserID().equals(newBean.getUserID());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            FriendBean oldBean = mOldData.get(oldItemPosition);
            FriendBean newBean = mNewData.get(oldItemPosition);
            if (!oldBean.getAvatar().equals(newBean.getAvatar())) {
                return false;
            }
            if (!oldBean.getRemarkName().equals(newBean.getRemarkName())) {
                return false;
            }
            if (!oldBean.getNickname().equals(newBean.getNickname())) {
                return false;
            }
            return true;
        }
    }
}
