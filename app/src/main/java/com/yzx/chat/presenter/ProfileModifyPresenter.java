package com.yzx.chat.presenter;

import android.support.annotation.NonNull;

import com.yzx.chat.contract.ProfileModifyContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.tool.ApiHelper;
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
    public void updateProfile(String nickname, int sex, String birthday, String location, String signature) {
        AsyncUtil.cancelCall(mUpdateUserProfileCall);
        mUpdateUserProfileCall = mUserApi.updateUserProfile(nickname, sex, birthday, location, signature);
        mUpdateUserProfileCall.setCallback(new HttpCallback<JsonResponse<Void>>() {
            @Override
            public void onResponse(HttpResponse<JsonResponse<Void>> response) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public boolean isExecuteNextTask() {
                return false;
            }
        });
        sHttpExecutor.submit(mUpdateUserProfileCall);
    }
}
