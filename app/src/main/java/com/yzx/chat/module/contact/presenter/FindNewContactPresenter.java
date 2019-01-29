package com.yzx.chat.module.contact.presenter;

import android.os.Handler;

import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.contact.contract.FindNewContactContract;
import com.yzx.chat.core.net.api.JsonResponse;
import com.yzx.chat.core.net.api.user.SearchUserBean;
import com.yzx.chat.core.net.api.user.UserApi;
import com.yzx.chat.core.IMClient;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AsyncUtil;

import java.util.List;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class FindNewContactPresenter implements FindNewContactContract.Presenter {

    private FindNewContactContract.View mFindNewContactContractView;

    private Call<JsonResponse<SearchUserBean>> mSearchUserCall;
    private UserApi mUserApi;
    private Handler mHandler;

    private boolean isSearching;

    @Override
    public void attachView(FindNewContactContract.View view) {
        mFindNewContactContractView = view;
        mUserApi = (UserApi) ApiHelper.getProxyInstance(UserApi.class);
        mHandler = new Handler();
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mSearchUserCall);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


    @Override
    public void searchUser(String nicknameOrTelephone) {
        if (isSearching) {
            return;
        }
        isSearching = true;
        AsyncUtil.cancelCall(mSearchUserCall);
        mSearchUserCall = mUserApi.searchUser(nicknameOrTelephone);
        mSearchUserCall.setResponseCallback(new BaseResponseCallback<SearchUserBean>() {
            @Override
            protected void onSuccess(SearchUserBean response) {
                List<UserEntity> userList = response.getUserList();
                if (userList == null || userList.size() == 0) {
                    mFindNewContactContractView.searchNotExist();
                } else {
                    UserEntity user = userList.get(0);
                    mFindNewContactContractView.searchSuccess(user, IMClient.getInstance().getContactManager().getContact(user.getUserID()) != null);
                }
                isSearching = false;

            }

            @Override
            protected void onFailure(String message) {
                mFindNewContactContractView.searchFail();
                isSearching = false;
            }
        });
        sHttpExecutor.submit(mSearchUserCall);

    }
}
