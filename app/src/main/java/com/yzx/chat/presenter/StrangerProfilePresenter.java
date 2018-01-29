package com.yzx.chat.presenter;

import android.support.annotation.NonNull;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.StrangerProfileContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class StrangerProfilePresenter implements StrangerProfileContract.Presenter {

    private StrangerProfileContract.View mStrangerProfileView;
    private Call<JsonResponse<Void>> mRequestContactCall;

    private UserApi mUserApi;

    @Override
    public void attachView(StrangerProfileContract.View view) {
        mStrangerProfileView = view;
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {
        mStrangerProfileView = null;
    }

    @Override
    public void requestContact(String userID, String verifyContent) {
        NetworkUtil.cancelCall(mRequestContactCall);
        mRequestContactCall = mUserApi.requestContact(userID, verifyContent);
        mRequestContactCall.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                mStrangerProfileView.goBack();
            }

            @Override
            protected void onFailure(String message) {

            }
        });
        sHttpExecutor.submit(mRequestContactCall);
    }
}
