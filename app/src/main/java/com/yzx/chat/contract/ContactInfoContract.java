package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;

/**
 * Created by YZX on 2018年01月21日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactInfoContract {

    public interface View extends BaseView<Presenter> {

        void switchTopState(boolean isOpen);

        void switchRemindState(boolean isOpen);

        void updateContactInfo(ContactBean contact);

    }


    public interface Presenter extends BasePresenter<View> {
        void init(String contactID);

        void enableConversationNotification(boolean isEnable);

        void setConversationToTop( boolean isTop);

        void clearChatMessages();
    }
}
