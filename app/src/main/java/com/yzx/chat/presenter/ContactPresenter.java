package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.util.NetworkAsyncTask;
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
    private List<ContactBean> mContactList;
    private Handler mHandler;

    private UserApi mUserApi;
    private Call<JsonResponse<Void>> mUpdateRemarkNameCall;

    @Override
    public void attachView(ContactContract.View view) {
        mContactView = view;
        mHandler = new Handler();
        mContactList = new ArrayList<>(128);
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        NetworkUtil.cancelTask(mRefreshContactsTask);
        mContactView = null;
        mHandler.removeCallbacksAndMessages(null);
        mContactList.clear();
        mContactList = null;
        mHandler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllContact(List<ContactBean> oldData) {
        NetworkUtil.cancelTask(mLoadUnreadCountTask);
        mLoadUnreadCountTask = new LoadUnreadCountTask();
        mLoadUnreadCountTask.execute();

        NetworkUtil.cancelTask(mRefreshContactsTask);
        mRefreshContactsTask = new RefreshAllContactsTask(this);
        mRefreshContactsTask.execute(oldData, mContactList);

    }

    @Override
    public void updateRemarkName(final ContactBean contactBean, final String newRemarkName) {
        NetworkUtil.cancelCall(mUpdateRemarkNameCall);
        mUpdateRemarkNameCall = mUserApi.updateRemarkName(contactBean.getUserID(), newRemarkName);
        mUpdateRemarkNameCall.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                contactBean.setRemarkName(newRemarkName);
                IMClient.getInstance().contactManager().updateContact(contactBean);
                mContactView.updateContact(contactBean);
            }

            @Override
            protected void onFailure(String message) {

            }
        });
    }

    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mContactView.updateContactListView(diffResult, mContactList);
    }


    private static class RefreshAllContactsTask extends NetworkAsyncTask<ContactPresenter, List<ContactBean>, DiffUtil.DiffResult> {

        RefreshAllContactsTask(ContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactBean>[] lists) {
            List<ContactBean> newList = lists[1];
            newList.clear();
            newList.addAll(IMClient.getInstance().contactManager().getAllContacts());

            Collections.sort(lists[1], new Comparator<ContactBean>() {
                @Override
                public int compare(ContactBean o1, ContactBean o2) {
                    if (o2 != null && o1 != null) {
                        return o1.getAbbreviation().compareTo(o2.getAbbreviation());
                    } else {
                        return 0;
                    }
                }
            });

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactBean>(lists[0], lists[1]) {
                @Override
                public boolean isItemEquals(ContactBean oldItem, ContactBean newItem) {
                    return oldItem.getUserID().equals(newItem.getUserID());
                }

                @Override
                public boolean isContentsEquals(ContactBean oldItem, ContactBean newItem) {
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
