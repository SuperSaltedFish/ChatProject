package com.yzx.chat.module.contact.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;

import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationContract {

    public interface View extends BaseView<Presenter> {
        void addContactOperationToList(ContactOperationEntity ContactOperation);

        void removeContactOperationFromList(ContactOperationEntity ContactOperation);

        void updateContactOperationFromList(ContactOperationEntity ContactOperation);

        void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationEntity> newDataList);

        void setEnableProgressDialog(boolean isEnable);

        void showError(String error);

    }


    public interface Presenter extends BasePresenter<View> {

        void init();

        void acceptContactRequest(ContactOperationEntity contactOperation);

        void refusedContactRequest(ContactOperationEntity contactOperation);

        void removeContactOperation(ContactOperationEntity ContactOperation);

        void loadAllContactOperation();

        ContactEntity findContact(String userID);

    }

}
