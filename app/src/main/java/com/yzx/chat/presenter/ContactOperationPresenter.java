package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.contract.ContactOperationContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AsyncResult;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationPresenter implements ContactOperationContract.Presenter {

    private ContactOperationContract.View mContactOperationContractView;
    private LoadAllContactOperationTask mLoadAllContactOperationTask;
    private List<ContactOperationBean> mContactOperationList;
    private AcceptContactResult mAcceptContactResult;
    private IMClient mIMClient;
    private Handler mHandler;

    @Override
    public void attachView(ContactOperationContract.View view) {
        mContactOperationContractView = view;
        mContactOperationList = new ArrayList<>(32);
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactOperationListener(mOnContactOperationListener);
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mLoadAllContactOperationTask);
        AsyncUtil.cancelResult(mAcceptContactResult);
        mIMClient.contactManager().removeContactOperationListener(mOnContactOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContactOperationContractView = null;
        mIMClient = null;
    }

    @Override
    public void init() {
        mIMClient.contactManager().makeAllContactOperationAsRead();
        loadAllContactOperation();
    }

    @Override
    public void acceptContactRequest(final ContactOperationBean contactOperation) {
        AsyncUtil.cancelResult(mAcceptContactResult);
        mContactOperationContractView.enableProgressDialog(true);
        mAcceptContactResult = new AcceptContactResult(this);
        mIMClient.contactManager().acceptContact(contactOperation, mAcceptContactResult);
    }

    @Override
    public void removeContactOperation(ContactOperationBean ContactOperation) {
        mIMClient.contactManager().removeContactOperationAsync(ContactOperation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContactOperation() {
        AsyncUtil.cancelTask(mLoadAllContactOperationTask);
        mLoadAllContactOperationTask = new LoadAllContactOperationTask(this);
        mLoadAllContactOperationTask.execute(mContactOperationList);
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
                    int index = mContactOperationList.indexOf(message);
                    if (index < 0) {
                        mContactOperationList.add(0, message);
                        mContactOperationContractView.addContactOperationToList(message);
                    } else {
                        mContactOperationList.set(index, message);
                        mContactOperationContractView.updateContactOperationFromList(message);
                    }
                }
            });
        }

        @Override
        public void onContactOperationUpdate(final ContactOperationBean message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int index = mContactOperationList.indexOf(message);
                    if (index < 0) {
                        loadAllContactOperation();
                    } else {
                        mContactOperationList.set(index, message);
                    }
                    mContactOperationContractView.updateContactOperationFromList(message);
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
                }
            });
        }
    };

    public void acceptContactRequestSuccess() {
        mContactOperationContractView.enableProgressDialog(false);
    }

    public void acceptContactRequestFailure(String error) {
        mContactOperationContractView.showError(error);
        mContactOperationContractView.enableProgressDialog(false);
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
                    return oldItem.getUserID().equals(newItem.getUserID());
                }

                @Override
                public boolean isContentsEquals(ContactOperationBean oldItem, ContactOperationBean newItem) {
                    return oldItem.getType().equals(newItem.getType()) && oldItem.getReason().equals(newItem.getReason());
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

    private static class AcceptContactResult extends AsyncResult<ContactOperationPresenter, Boolean> {

        public AcceptContactResult(ContactOperationPresenter dependent) {
            super(dependent);
        }

        @Override
        protected void onSuccessResult(ContactOperationPresenter dependent, Boolean result) {
            dependent.acceptContactRequestSuccess();
        }

        @Override
        protected void onFailureResult(ContactOperationPresenter dependent, String error) {
            dependent.acceptContactRequestFailure(error);
        }
    }


}
