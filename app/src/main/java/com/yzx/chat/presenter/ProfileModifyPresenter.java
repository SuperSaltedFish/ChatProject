package com.yzx.chat.presenter;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.ProfileModifyContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;


/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ProfileModifyPresenter implements ProfileModifyContract.Presenter {
    private ProfileModifyContract.View mProfileModifyView;

    @Override
    public void attachView(ProfileModifyContract.View view) {
        mProfileModifyView = view;
    }

    @Override
    public void detachView() {
        mProfileModifyView = null;
    }

    @Override
    public void updateProfile(final UserBean user) {
        IMClient.getInstance().userManager().updateProfile(user.getNickname(), user.getSex(), user.getBirthday(), user.getLocation(), user.getSignature(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (mProfileModifyView != null) {
                    mProfileModifyView.goBack();
                }
            }

            @Override
            public void onFailure(String error) {
                if (mProfileModifyView != null) {
                    mProfileModifyView.showError(error);
                }
            }
        });

    }

}
