package com.yzx.chat.presenter;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.StrangerProfileContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.framework.Call;
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
    public void requestContact(final UserBean user, final String verifyContent) {
        NetworkUtil.cancelCall(mRequestContactCall);
        mRequestContactCall = mUserApi.requestContact(user.getUserID(), verifyContent);
        mRequestContactCall.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                ContactOperationBean operation = new ContactOperationBean();
                operation.setReason(verifyContent);
                operation.setUserID(user.getUserID());
                operation.setTime((int) (System.currentTimeMillis() / 1000));
                operation.setUser(user);
                operation.setType(ContactManager.CONTACT_OPERATION_VERIFYING);
                IMClient.getInstance().contactManager().replaceContactOperationAsync(operation);
                mStrangerProfileView.goBack();
            }

            @Override
            protected void onFailure(String message) {

            }
        });
        sHttpExecutor.submit(mRequestContactCall);
    }
}
