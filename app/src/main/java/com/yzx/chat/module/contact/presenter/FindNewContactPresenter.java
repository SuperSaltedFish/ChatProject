package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.contact.contract.FindNewContactContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

import java.util.List;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class FindNewContactPresenter implements FindNewContactContract.Presenter {

    private FindNewContactContract.View mFindNewContactContractView;

    @Override
    public void attachView(FindNewContactContract.View view) {
        mFindNewContactContractView = view;
    }

    @Override
    public void detachView() {
    }


    @Override
    public void searchUser(String nicknameOrTelephone) {
        AppClient.getInstance().getUserManager().searchUser(nicknameOrTelephone, new LifecycleMVPResultCallback<List<UserEntity>>(mFindNewContactContractView) {
            @Override
            protected void onSuccess(List<UserEntity> result) {
                if (result == null || result.size() == 0) {
                    mFindNewContactContractView.searchNotExist();
                } else {
                    UserEntity user = result.get(0);
                    mFindNewContactContractView.showSearchResult(user, AppClient.getInstance().getContactManager().getContact(user.getUserID()) != null);
                }
            }
        });
    }
}
