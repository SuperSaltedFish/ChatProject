package com.yzx.chat.module.contact.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.UserEntity;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class FindNewContactContract {
    public interface View extends BaseView<Presenter> {
        void showSearchResult(UserEntity user, boolean isContact);

        void searchNotExist();
    }


    public interface Presenter extends BasePresenter<View> {

        void searchUser(String nicknameOrTelephone);
    }
}
