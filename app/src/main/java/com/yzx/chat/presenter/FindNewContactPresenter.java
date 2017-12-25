package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.FindNewContactContract;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.SearchUserBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class FindNewContactPresenter implements FindNewContactContract.Presenter {

    private FindNewContactContract.View mFindNewContactContractView;
    private LoadContactRequestTask mLoadContactRequestTask;
    private RequestAddContactTask mRequestAddContactTask;
    private Call<JsonResponse<Void>> mAddFriendCall;
    private Call<JsonResponse<SearchUserBean>> mSearchUserCall;
    private UserApi mUserApi;
    private ChatClientManager mChatManager;
    private Handler mHandler;

    private List<ContactBean> mContactList;

    @Override
    public void attachView(FindNewContactContract.View view) {
        mFindNewContactContractView = view;
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        mChatManager = ChatClientManager.getInstance();
        mHandler = new Handler();
        mContactList = new ArrayList<>(32);
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelTask(mLoadContactRequestTask);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mFindNewContactContractView = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadContactRequest(List<ContactBean> mOldContactList) {
        NetworkUtil.cancelTask(mLoadContactRequestTask);
        mLoadContactRequestTask = new LoadContactRequestTask(this);
        mLoadContactRequestTask.execute(mOldContactList, mContactList);
    }

    @Override
    public void requestAddContact(String contactID, String reason) {
        NetworkUtil.cancelTask(mRequestAddContactTask);
        mRequestAddContactTask = new RequestAddContactTask(this);
        mRequestAddContactTask.execute(contactID, reason);
    }

    @Override
    public void acceptContactRequest(final String contactID) {


    }

    @Override
    public void searchUser(String nicknameOrTelephone) {
        NetworkUtil.cancelCall(mSearchUserCall);
        mSearchUserCall = mUserApi.searchUser(nicknameOrTelephone);
        mSearchUserCall.setCallback(new BaseHttpCallback<SearchUserBean>() {
            @Override
            protected void onSuccess(SearchUserBean response) {

            }

            @Override
            protected void onFailure(String message) {

            }
        });
    }

    private void loadContactRequestComplete(DiffUtil.DiffResult diffResult) {

    }

    private void requestAddContactSuccess() {

    }

    private void requestAddContactFail(String error) {

    }

    private static class LoadContactRequestTask extends NetworkAsyncTask<FindNewContactPresenter, List<ContactBean>, DiffUtil.DiffResult> {

        LoadContactRequestTask(FindNewContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ContactBean>[] lists) {
            ContactDao dao = DBManager.getInstance().getContactDao();
            List<ContactBean> newList = lists[1];
            newList.clear();
            newList.addAll(dao.loadAllContactInfo(IdentityManager.getInstance().getUserID()));

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ContactBean>(lists[0], newList) {
                @Override
                public boolean isItemEquals(ContactBean oldItem, ContactBean newItem) {
                    return oldItem.getUserFrom().equals(newItem.getUserFrom());
                }

                @Override
                public boolean isContentsEquals(ContactBean oldItem, ContactBean newItem) {
                    if (oldItem.isRemind() != newItem.isRemind()) {
                        return false;
                    }
                    if (oldItem.getTime() != newItem.getTime()) {
                        return false;
                    }
                    if (!oldItem.getType().equals(newItem.getType())) {
                        return false;
                    }
                    if (!oldItem.getReason().equals(newItem.getReason())) {
                        return false;
                    }
                    return true;
                }
            }, true);

            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, FindNewContactPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.loadContactRequestComplete(diffResult);
        }
    }

    private static class RequestAddContactTask extends NetworkAsyncTask<FindNewContactPresenter, String, String> {

        RequestAddContactTask(FindNewContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(String... params) {

            return AndroidUtil.getString(R.string.FindNewContactPresenter_AddFriendFail);
        }

        @Override
        protected void onPostExecute(String result, FindNewContactPresenter lifeDependentObject) {
            super.onPostExecute(result, lifeDependentObject);
            if (result == null) {
                lifeDependentObject.requestAddContactSuccess();
            } else {
                lifeDependentObject.requestAddContactFail(result);
            }
        }
    }
}
