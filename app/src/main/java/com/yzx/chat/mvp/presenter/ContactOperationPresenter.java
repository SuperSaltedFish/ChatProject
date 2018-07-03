package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.mvp.contract.ContactOperationContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;

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
    private IMClient mIMClient;
    private Handler mHandler;

    @Override
    public void attachView(ContactOperationContract.View view) {
        mContactOperationContractView = view;
        mContactOperationList = new ArrayList<>(32);
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.getContactManager().addContactOperationListener(mOnContactOperationListener);
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mLoadAllContactOperationTask);
        mIMClient.getContactManager().removeContactOperationListener(mOnContactOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContactOperationContractView = null;
        mIMClient = null;
    }

    @Override
    public void init() {
        mIMClient.getContactManager().makeAllContactOperationAsRead();
        loadAllContactOperation();

    }

    @Override
    public void acceptContactRequest(final ContactOperationBean contactOperation) {
        mContactOperationContractView.setEnableProgressDialog(true);
        mIMClient.getContactManager().acceptContact(contactOperation, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mContactOperationContractView.setEnableProgressDialog(false);
            }

            @Override
            public void onFailure(String error) {
                mContactOperationContractView.showError(error);
                mContactOperationContractView.setEnableProgressDialog(false);
            }
        });
    }

    @Override
    public void removeContactOperation(ContactOperationBean ContactOperation) {
        mIMClient.getContactManager().removeContactOperationAsync(ContactOperation);
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
            mIMClient.getContactManager().makeAllContactOperationAsRead();
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
        public void onContactOperationRemove(final ContactOperationBean message) {
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


    private static class LoadAllContactOperationTask extends BackstageAsyncTask<ContactOperationPresenter, List<ContactOperationBean>, DiffUtil.DiffResult> {

        LoadAllContactOperationTask(ContactOperationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactOperationBean>[] lists) {
            List<ContactOperationBean> oldList = lists[0];
            List<ContactOperationBean> newList = IMClient.getInstance().getContactManager().loadAllContactOperation();
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



}
