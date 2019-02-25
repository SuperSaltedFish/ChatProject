package com.yzx.chat.module.contact.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactChatSettingContract {

    public interface View extends BaseView<ContactChatSettingContract.Presenter> {

        void updateContactInfo(ContactEntity contact);

        void switchTopState(boolean isOpen);

        void switchRemindState(boolean isOpen);

    }


    public interface Presenter extends BasePresenter<ContactChatSettingContract.View> {
        void init(String contactID);

        ContactEntity getContact();

        void enableConversationNotification(boolean isEnable);

        void setConversationToTop(boolean isTop);

        void clearChatMessages();

    }
}
