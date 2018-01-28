package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.FindNewContactContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.GetUserProfileBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class FindNewContactPresenter implements FindNewContactContract.Presenter {

    private FindNewContactContract.View mFindNewContactContractView;

    private Call<JsonResponse<UserBean>> mSearchUserCall;
    private Call<JsonResponse<GetUserProfileBean>> mGetUserProfileCall;
    private UserApi mUserApi;
    private Handler mHandler;

    private boolean isSearching;

    @Override
    public void attachView(FindNewContactContract.View view) {
        mFindNewContactContractView = view;
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        mHandler = new Handler();
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelCall(mSearchUserCall);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


    @Override
    public void searchUser(String nicknameOrTelephone) {
        if (isSearching) {
            return;
        }
        isSearching = true;
//        NetworkUtil.cancelCall(mSearchUserCall);
//        mSearchUserCall = mUserApi.searchUser(nicknameOrTelephone);
//        mSearchUserCall.setCallback(new BaseHttpCallback<UserBean>() {
//            @Override
//            protected void onSuccess(UserBean response) {
//
//                isSearching = false;
//
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                mFindNewContactContractView.searchFail();
//                isSearching = false;
//            }
//        });
//        sHttpExecutor.submit(mSearchUserCall);

        mGetUserProfileCall = mUserApi.getUserProfile(nicknameOrTelephone);
        mGetUserProfileCall.setCallback(new BaseHttpCallback<GetUserProfileBean>() {
            @Override
            protected void onSuccess(GetUserProfileBean response) {

            }

            @Override
            protected void onFailure(String message) {

            }
        });
        sHttpExecutor.submit(mGetUserProfileCall);
    }
}
