package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class StrangerProfileContract {

    public interface View extends BaseView<Presenter> {
        void goBack();
        void showError(String error);
    }


    public interface Presenter extends BasePresenter<View> {

        void requestContact(UserBean user, String verifyContent);
    }
}
