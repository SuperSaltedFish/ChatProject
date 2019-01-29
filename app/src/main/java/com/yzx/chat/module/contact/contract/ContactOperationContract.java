package com.yzx.chat.module.contact.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactOperationBean;

import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationContract {

    public interface View extends BaseView<Presenter> {
        void addContactOperationToList(ContactOperationBean ContactOperation);

        void removeContactOperationFromList(ContactOperationBean ContactOperation);

        void updateContactOperationFromList(ContactOperationBean ContactOperation);

        void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationBean> newDataList);

        void setEnableProgressDialog(boolean isEnable);

        void showError(String error);

    }


    public interface Presenter extends BasePresenter<View> {

        void init();

        void acceptContactRequest(ContactOperationBean contactOperation);

        void refusedContactRequest(ContactOperationBean contactOperation);

        void removeContactOperation(ContactOperationBean ContactOperation);

        void loadAllContactOperation();

        ContactBean findContact(String userID);

    }

}
