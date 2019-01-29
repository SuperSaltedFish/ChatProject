package com.yzx.chat.module.contact.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfileContract {

    public interface View extends BaseView<Presenter> {
        void updateContactInfo(ContactEntity contact);

        void showError(String error);

        void goBack();

        void setEnableProgressDialog(boolean isEnable);

    }


    public interface Presenter extends BasePresenter<View> {
        void init(String contactID);

        ContactEntity getContact();

        void deleteContact();

        ArrayList<String> getAllTags();

        void saveRemarkInfo(ContactEntity contact);
    }
}
