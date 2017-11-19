package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactPresenter implements ContactContract.Presenter {

    private ContactContract.View mContactView;
    private LoadUnreadCountTask mLoadUnreadCountTask;
    private Handler mHandler;

    @Override
    public void attachView(ContactContract.View view) {
        mContactView = view;
        mHandler = new Handler();
        ChatClientManager.getInstance().addUnreadCountChangeListener(mUnreadCountChangeListener);
    }

    @Override
    public void detachView() {
        mContactView = null;
        ChatClientManager.getInstance().removeUnreadCountChangeListener(mUnreadCountChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler=null;
    }

    @Override
    public void loadAllContact() {
        NetworkUtil.cancel(mLoadUnreadCountTask);
        mLoadUnreadCountTask = new LoadUnreadCountTask(this);
        mLoadUnreadCountTask.execute();
    }

    @Override
    public void loadUnreadComplete(int count) {
        mContactView.updateUnreadBadge(count);
        ChatClientManager.getInstance().setContactUnreadCount(count);
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

    private static class LoadUnreadCountTask extends NetworkAsyncTask<Void, Integer> {

        LoadUnreadCountTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int count = DBManager.getInstance().getContactDao().loadRemindCount();
            ChatClientManager.getInstance().setContactUnreadCount(count);
            return count;
        }

        @Override
        protected void onPostExecute(Integer integer, Object lifeCycleObject) {
            super.onPostExecute(integer, lifeCycleObject);
            ContactPresenter presenter = (ContactPresenter) lifeCycleObject;
            presenter.loadUnreadComplete(integer);
        }
    }
}
