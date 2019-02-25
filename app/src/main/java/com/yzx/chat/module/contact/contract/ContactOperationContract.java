package com.yzx.chat.module.contact.contract;


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
        void showContactOperation(List<ContactOperationEntity> contactOperationList);
    }


    public interface Presenter extends BasePresenter<View> {

        void loadAllAndMakeAllAsRead();

        void acceptContactRequest(ContactOperationEntity contactOperation);

        void refusedContactRequest(ContactOperationEntity contactOperation);

        void removeContactOperation(ContactOperationEntity ContactOperation);

        ContactEntity findContact(String userID);

    }

}
