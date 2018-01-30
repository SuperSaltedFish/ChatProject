package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ContactOperationContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkAsyncTask;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactOperationPresenter implements ContactOperationContract.Presenter {

    private ContactOperationContract.View mContactOperationContractView;
    private LoadAllContactOperationTask mLoadAllContactOperationTask;
    private LoadMoreContactOperationTask mLoadMoreContactOperationTask;
    private List<ContactOperationBean> mContactOperationList;
    private IMClient mIMClient;
    private Handler mHandler;

    private String mUserID;

    private boolean isLoadingMore;
    private boolean hasLoadingMore;

    @Override
    public void attachView(ContactOperationContract.View view) {
        mContactOperationContractView = view;
        mContactOperationList = new ArrayList<>(32);
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactOperationListener(mOnContactOperationListener);
        hasLoadingMore = true;
    }

    @Override
    public void detachView() {
        mIMClient.contactManager().removeContactOperationListener(mOnContactOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContactOperationContractView = null;
        mIMClient = null;
    }

    @Override
    public void init(String userID) {
        mUserID = userID;
        if (!TextUtils.isEmpty(mUserID)) {
            mIMClient.contactManager().makeAllContactOperationAsRead();
            loadMoreContactOperation(Integer.MAX_VALUE);
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
    public void removeContactOperation(ContactOperationBean ContactOperation) {
        mIMClient.contactManager().removeContactOperationAsync(ContactOperation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContactOperation() {
        NetworkUtil.cancelTask(mLoadAllContactOperationTask);
        mLoadAllContactOperationTask = new LoadAllContactOperationTask(this);
        mLoadAllContactOperationTask.execute(mContactOperationList);
    }

    @Override
    public void loadMoreContactOperation(int startID) {
        NetworkUtil.cancelTask(mLoadMoreContactOperationTask);
        mLoadMoreContactOperationTask = new LoadMoreContactOperationTask(this);
        mLoadMoreContactOperationTask.execute(startID, Constants.CONTACT_MESSAGE_PAGE_SIZE);
        isLoadingMore = true;
    }

    public void loadMoreContactOperationComplete(List<ContactOperationBean> ContactOperationList) {
        if (ContactOperationList == null) {
            hasLoadingMore = false;
        } else {
            mContactOperationList.addAll(ContactOperationList);
            if (ContactOperationList.size() < Constants.CONTACT_MESSAGE_PAGE_SIZE) {
                hasLoadingMore = false;
            } else {
                hasLoadingMore = true;
            }
        }
        mContactOperationContractView.addMoreContactOperationToList(ContactOperationList, hasLoadingMore);
        mContactOperationContractView.enableLoadMoreHint(mContactOperationList.size() >= Constants.CONTACT_MESSAGE_PAGE_SIZE);
        isLoadingMore = false;
    }

    public void loadAllContactOperationComplete(DiffUtil.DiffResult diffResult) {
        mContactOperationContractView.updateAllContactOperationList(diffResult, mContactOperationList);
    }

    private final ContactManager.OnContactOperationListener mOnContactOperationListener = new ContactManager.OnContactOperationListener() {

        @Override
        public void onContactOperationReceive(final ContactOperationBean message) {
            mIMClient.contactManager().makeAllContactOperationAsRead();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContactOperationList.add(0, message);
                    mContactOperationContractView.addContactOperationToList(message);
                }
            });
        }

        @Override
        public void onContactOperationDelete(final ContactOperationBean message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContactOperationList.remove(message)) {
                        LogUtil.e("remove ContactOperation fail from mContactOperationList");
                    }
                    mContactOperationContractView.removeContactOperationFromList(message);
                    mContactOperationContractView.enableLoadMoreHint(mContactOperationList.size() >= Constants.CONTACT_MESSAGE_PAGE_SIZE);
                }
            });
        }
    };


    private static class LoadMoreContactOperationTask extends NetworkAsyncTask<ContactOperationPresenter, Integer, List<ContactOperationBean>> {


        LoadMoreContactOperationTask(ContactOperationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected List<ContactOperationBean> doInBackground(Integer... params) {
            return IMClient.getInstance().contactManager().loadMoreContactOperation(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(List<ContactOperationBean> ContactOperationList, ContactOperationPresenter lifeDependentObject) {
            super.onPostExecute(ContactOperationList, lifeDependentObject);
            lifeDependentObject.loadMoreContactOperationComplete(ContactOperationList);
        }
    }

    private static class LoadAllContactOperationTask extends NetworkAsyncTask<ContactOperationPresenter, List<ContactOperationBean>, DiffUtil.DiffResult> {

        LoadAllContactOperationTask(ContactOperationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactOperationBean>[] lists) {
            List<ContactOperationBean> oldList = lists[0];
            List<ContactOperationBean> newList = IMClient.getInstance().contactManager().loadAllContactOperation();
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactOperationBean>(lists[0], newList) {
                @Override
                public boolean isItemEquals(ContactOperationBean oldItem, ContactOperationBean newItem) {
                    return oldItem.getUserFrom().equals(newItem.getUserFrom());
                }

                @Override
                public boolean isContentsEquals(ContactOperationBean oldItem, ContactOperationBean newItem) {
                    return oldItem.getType() == newItem.getType();
                }
            });
            oldList.clear();
            oldList.addAll(newList);
            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ContactOperationPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.loadAllContactOperationComplete(diffResult);
        }

    }


}
