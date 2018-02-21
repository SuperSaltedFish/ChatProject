package com.yzx.chat.presenter;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.ProfileModifyContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.AsyncUtil;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ProfileModifyPresenter implements ProfileModifyContract.Presenter {
    private ProfileModifyContract.View mProfileModifyView;
    private Call<JsonResponse<Void>> mUpdateUserProfileCall;

    private UserApi mUserApi;

    @Override
    public void attachView(ProfileModifyContract.View view) {
        mProfileModifyView = view;
        mUserApi = (UserApi) ApiHelper.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {
        mProfileModifyView = null;
    }

    @Override
    public void updateProfile(final UserBean user) {
        AsyncUtil.cancelCall(mUpdateUserProfileCall);
        mUpdateUserProfileCall = mUserApi.updateUserProfile(user.getNickname(), user.getSex(), user.getBirthday(), user.getLocation(), user.getSignature());
        mUpdateUserProfileCall.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                IdentityManager.getInstance().updateUserInfo(user);
                mProfileModifyView.goBack();
            }

            @Override
            protected void onFailure(String message) {
                mProfileModifyView.showError(message);
            }
        });
        sHttpExecutor.submit(mUpdateUserProfileCall);
    }

}
