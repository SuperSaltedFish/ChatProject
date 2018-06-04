package com.yzx.chat.mvp.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfileContract {

    public interface View extends BaseView<Presenter> {
        void updateContactInfo(ContactBean contact);

        void finishChatActivity();

        void showError(String error);

        void goBack();

        void enableProgressDialog(boolean isEnable);
    }


    public interface Presenter extends BasePresenter<View> {
        void init(String contactID);

        ContactBean getContact();

        void deleteContact();
    }
}
