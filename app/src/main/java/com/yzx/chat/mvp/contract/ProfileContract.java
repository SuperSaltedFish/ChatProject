package com.yzx.chat.mvp.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年08月18日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class ProfileContract {
    public interface View extends BaseView<Presenter> {
        void showUserInfo(UserBean user);
    }


    public interface Presenter extends BasePresenter<View> {

        void initUserInfo();

    }
}
