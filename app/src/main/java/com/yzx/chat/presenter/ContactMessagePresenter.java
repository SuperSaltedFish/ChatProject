package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ContactMessageContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkAsyncTask;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactMessagePresenter implements ContactMessageContract.Presenter {

    private ContactMessageContract.View mContactMessageContractView;
    private LoadAllContactMessageTask mLoadAllContactMessageTask;
    private LoadMoreContactMessageTask mLoadMoreContactMessageTask;
    private List<ContactMessageBean> mContactMessageList;
    private IMClient mIMClient;
    private Handler mHandler;

    private String mUserID;

    private boolean isLoadingMore;
    private boolean hasLoadingMore;

    @Override
    public void attachView(ContactMessageContract.View view) {
        mContactMessageContractView = view;
        mContactMessageList = new ArrayList<>(32);
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactMessageListener(mOnContactMessageListener);
        hasLoadingMore = true;
    }

    @Override
    public void detachView() {
        mIMClient.contactManager().removeContactMessageListener(mOnContactMessageListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContactMessageContractView = null;
        mIMClient = null;
    }

    @Override
    public void init(String userID) {
        mUserID = userID;
        if (!TextUtils.isEmpty(mUserID)) {
            mIMClient.contactManager().makeAllContactMessageAsRead();
            loadMoreContactMessage(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    @Override
    public boolean hasMoreMessage() {
        return hasLoadingMore;
    }

    @Override
    public void removeContactMessage(ContactMessageBean contactMessage) {
        mIMClient.contactManager().removeContactMessageAsync(contactMessage);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContactMessage() {
        NetworkUtil.cancelTask(mLoadAllContactMessageTask);
        mLoadAllContactMessageTask = new LoadAllContactMessageTask(this);
        mLoadAllContactMessageTask.execute(mContactMessageList);
    }

    @Override
    public void loadMoreContactMessage(int startID) {
        NetworkUtil.cancelTask(mLoadMoreContactMessageTask);
        mLoadMoreContactMessageTask = new LoadMoreContactMessageTask(this);
        mLoadMoreContactMessageTask.execute(startID, Constants.CONTACT_MESSAGE_PAGE_SIZE);
        isLoadingMore = true;
    }

    public void loadMoreContactMessageComplete(List<ContactMessageBean> contactMessageList) {
        if (contactMessageList == null) {
            hasLoadingMore = false;
        } else {
            mContactMessageList.addAll(contactMessageList);
            if (contactMessageList.size() < Constants.CONTACT_MESSAGE_PAGE_SIZE) {
                hasLoadingMore = false;
            } else {
                hasLoadingMore = true;
            }
        }
        mContactMessageContractView.addMoreContactMessageToList(contactMessageList, hasLoadingMore);
        mContactMessageContractView.enableLoadMoreHint(mContactMessageList.size() >= Constants.CONTACT_MESSAGE_PAGE_SIZE);
        isLoadingMore = false;
    }

    public void loadAllContactMessageComplete(DiffUtil.DiffResult diffResult) {
        mContactMessageContractView.updateAllContactMessageList(diffResult, mContactMessageList);
    }

    private final ContactManager.OnContactMessageListener mOnContactMessageListener = new ContactManager.OnContactMessageListener() {

        @Override
        public void onContactMessageReceive(final ContactMessageBean message) {
            mIMClient.contactManager().makeAllContactMessageAsRead();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContactMessageList.add(0, message);
                    mContactMessageContractView.addContactMessageToList(message);
                }
            });
        }

        @Override
        public void onContactMessageDelete(final ContactMessageBean message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContactMessageList.remove(message)) {
                        LogUtil.e("remove contactMessage fail from mContactMessageList");
                    }
                    mContactMessageContractView.removeContactMessageFromList(message);
                    mContactMessageContractView.enableLoadMoreHint(mContactMessageList.size() >= Constants.CONTACT_MESSAGE_PAGE_SIZE);
                }
            });
        }
    };


    private static class LoadMoreContactMessageTask extends NetworkAsyncTask<ContactMessagePresenter, Integer, List<ContactMessageBean>> {


        LoadMoreContactMessageTask(ContactMessagePresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected List<ContactMessageBean> doInBackground(Integer... params) {
            return IMClient.getInstance().contactManager().loadMoreContactMessage(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(List<ContactMessageBean> contactMessageList, ContactMessagePresenter lifeDependentObject) {
            super.onPostExecute(contactMessageList, lifeDependentObject);
            lifeDependentObject.loadMoreContactMessageComplete(contactMessageList);
        }
    }

    private static class LoadAllContactMessageTask extends NetworkAsyncTask<ContactMessagePresenter, List<ContactMessageBean>, DiffUtil.DiffResult> {

        LoadAllContactMessageTask(ContactMessagePresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactMessageBean>[] lists) {
            List<ContactMessageBean> oldList = lists[0];
            List<ContactMessageBean> newList = IMClient.getInstance().contactManager().loadAllContactMessage();
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactMessageBean>(lists[0], newList) {
                @Override
                public boolean isItemEquals(ContactMessageBean oldItem, ContactMessageBean newItem) {
                    return oldItem.getUserFrom().equals(newItem.getUserFrom());
                }

                @Override
                public boolean isContentsEquals(ContactMessageBean oldItem, ContactMessageBean newItem) {
                    return oldItem.getType() == newItem.getType();
                }
            });
            oldList.clear();
            oldList.addAll(newList);
            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactMessagePresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.loadAllContactMessageComplete(diffResult);
        }

    }


}
