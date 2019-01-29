package com.yzx.chat.module.contact.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class StrangerProfileContract {

    public interface View extends BaseView<Presenter> {
        void goBack();
        void showError(String error);
        void setEnableProgressDialog(boolean isEnable);
    }


    public interface Presenter extends BasePresenter<View> {

        void requestContact(UserBean user, String verifyContent);

        void acceptContactRequest(ContactOperationBean contactOperation);
    }
}
