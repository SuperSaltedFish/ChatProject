package com.yzx.chat.mvp.presenter;

import com.yzx.chat.mvp.contract.ProfileContract;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2018年08月18日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class ProfilePresenter implements ProfileContract.Presenter {

    private ProfileContract.View mProfileView;

    @Override
    public void attachView(ProfileContract.View view) {
        mProfileView = view;
    }

    @Override
    public void detachView() {
        mProfileView =null;
    }

    @Override
    public void initUserInfo() {
        mProfileView.showUserInfo(IMClient.getInstance().getUserManager().getUser());
    }
}
