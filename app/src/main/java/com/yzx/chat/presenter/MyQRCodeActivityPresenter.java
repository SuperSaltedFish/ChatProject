package com.yzx.chat.presenter;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.MyQRCodeActivityContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.GetTempUserID;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AsyncUtil;

/**
 * Created by YZX on 2018年02月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeActivityPresenter implements MyQRCodeActivityContract.Presenter {

    private MyQRCodeActivityContract.View mMyQRCodeActivityView;
    private Call<JsonResponse<GetTempUserID>> mGetTempUserIDCall;
    private UserApi mUserApi;

    private boolean isUpdating;

    @Override
    public void attachView(MyQRCodeActivityContract.View view) {
        mMyQRCodeActivityView = view;
        mUserApi = (UserApi) ApiHelper.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {
        mMyQRCodeActivityView = null;
    }

    @Override
    public void updateQRCode() {
        if (isUpdating) {
            return;
        }
        isUpdating = true;
        AsyncUtil.cancelCall(mGetTempUserIDCall);
        mGetTempUserIDCall = mUserApi.getTempUserID();
        mGetTempUserIDCall.setCallback(new BaseHttpCallback<GetTempUserID>() {
            @Override
            protected void onSuccess(GetTempUserID response) {
                isUpdating = false;
                mMyQRCodeActivityView.showQRCode(response.getTempUserID());
            }

            @Override
            protected void onFailure(String message) {
                isUpdating = false;
                mMyQRCodeActivityView.showError(message);
            }
        });
        sHttpExecutor.submit(mGetTempUserIDCall);
    }
}
