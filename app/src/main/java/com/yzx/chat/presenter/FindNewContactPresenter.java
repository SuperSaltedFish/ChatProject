package com.yzx.chat.presenter;

import com.yzx.chat.contract.FindNewContactContract;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class FindNewContactPresenter implements FindNewContactContract.Presenter {

    private FindNewContactContract.View mFindNewContactContractView;
    private LoadFriendRequestTask mLoadFriendRequestTask;
    private UserApi mUserApi;

    @Override
    public void attachView(FindNewContactContract.View view) {
        mFindNewContactContractView = view;
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {
        mFindNewContactContractView = null;
    }

    @Override
    public void loadFriendRequest() {
        NetworkUtil.cancelTask(mLoadFriendRequestTask);
        mLoadFriendRequestTask = new LoadFriendRequestTask(this);
        mLoadFriendRequestTask.execute();
    }

    @Override
    public void acceptFriendRequest(String friendUserID) {

    }

    @Override
    public void searchUser(String nicknameOrTelephone) {

    }

    private static class LoadFriendRequestTask extends NetworkAsyncTask<FindNewContactPresenter,Void,String>{

        LoadFriendRequestTask(FindNewContactPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContactDao dao = DBManager.getInstance().getContactDao();
            dao.loadAllContactInfo(IdentityManager.getInstance().getUserID());
            return null;
        }
    }
}
