package com.yzx.chat.module.contact.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.module.contact.contract.ContactOperationContract;
import com.yzx.chat.core.manager.ContactManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.tool.NotificationHelper;
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
    private List<ContactOperationEntity> mContactOperationList;
    private AppClient mAppClient;
    private Handler mHandler;

    @Override
    public void attachView(ContactOperationContract.View view) {
        mContactOperationContractView = view;
        mContactOperationList = new ArrayList<>(32);
        mHandler = new Handler();
        mAppClient = AppClient.getInstance();
        mAppClient.getContactManager().addContactOperationListener(mOnContactOperationListener);
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mLoadAllContactOperationTask);
        mAppClient.getContactManager().removeContactOperationListener(mOnContactOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContactOperationContractView = null;
        mAppClient = null;
    }

    @Override
    public void init() {
        mAppClient.getContactManager().makeAllContactOperationAsRead();
        loadAllContactOperation();
        NotificationHelper.getInstance().cancelAllContactOperationNotification();
    }

    @Override
    public void acceptContactRequest(final ContactOperationEntity contactOperation) {
        mContactOperationContractView.setEnableProgressDialog(true);
        mAppClient.getContactManager().acceptContact(contactOperation, new ResultCallback<Void>() {
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
    public void refusedContactRequest(ContactOperationEntity contactOperation) {
        mContactOperationContractView.setEnableProgressDialog(true);
        mAppClient.getContactManager().refusedContact(contactOperation, "", new ResultCallback<Void>() {
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
    public void removeContactOperation(ContactOperationEntity ContactOperation) {
        mAppClient.getContactManager().removeContactOperationAsync(ContactOperation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllContactOperation() {
        AsyncUtil.cancelTask(mLoadAllContactOperationTask);
        mLoadAllContactOperationTask = new LoadAllContactOperationTask(this);
        mLoadAllContactOperationTask.execute(mContactOperationList);
    }

    @Override
    public ContactEntity findContact(String userID) {
        return mAppClient.getContactManager().getContact(userID);
    }


    public void loadAllContactOperationComplete(DiffUtil.DiffResult diffResult) {
        mContactOperationContractView.updateAllContactOperationList(diffResult, mContactOperationList);
    }

    private final ContactManager.OnContactOperationListener mOnContactOperationListener = new ContactManager.OnContactOperationListener() {

        @Override
        public void onContactOperationReceive(final ContactOperationEntity message) {
            mAppClient.getContactManager().makeAllContactOperationAsRead();
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
        public void onContactOperationUpdate(final ContactOperationEntity message) {
            mAppClient.getContactManager().makeAllContactOperationAsRead();
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
        public void onContactOperationRemove(final ContactOperationEntity message) {
            mAppClient.getContactManager().makeAllContactOperationAsRead();
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


    private static class LoadAllContactOperationTask extends BackstageAsyncTask<ContactOperationPresenter, List<ContactOperationEntity>, DiffUtil.DiffResult> {

        LoadAllContactOperationTask(ContactOperationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactOperationEntity>[] lists) {
            List<ContactOperationEntity> oldList = lists[0];
            List<ContactOperationEntity> newList = AppClient.getInstance().getContactManager().loadAllContactOperation();
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactOperationEntity>(lists[0], newList) {
                @Override
                public boolean isItemEquals(ContactOperationEntity oldItem, ContactOperationEntity newItem) {
                    return oldItem.getUserID().equals(newItem.getUserID());
                }

                @Override
                public boolean isContentsEquals(ContactOperationEntity oldItem, ContactOperationEntity newItem) {
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
