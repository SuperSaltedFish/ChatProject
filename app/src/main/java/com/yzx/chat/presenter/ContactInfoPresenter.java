package com.yzx.chat.presenter;


import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactInfoContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年01月21日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactInfoPresenter implements ContactInfoContract.Presenter {

    private ContactInfoContract.View mContactInfoContractView;
    private IMClient mIMClient;
    private ContactBean mContactBean;
    private Conversation mConversation;
    private Handler mHandler;

    @Override
    public void attachView(ContactInfoContract.View view) {
        mContactInfoContractView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.contactManager().removeContactChangeListener(mOnContactChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mContactInfoContractView = null;
        mIMClient = null;
    }


    @Override
    public void init(ContactBean contact) {
        mContactBean = contact;
        mConversation = mIMClient.conversationManager().getConversation(Conversation.ConversationType.PRIVATE, mContactBean.getUserID());
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(mContactBean.getUserID());
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.PRIVATE);
        }

        mContactInfoContractView.switchTopState(mConversation.isTop());
        mIMClient.conversationManager().isEnableConversationNotification(mConversation, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                mContactInfoContractView.switchRemindState(conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mIMClient.conversationManager().enableConversationNotification(mConversation, isEnable);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mIMClient.conversationManager().setConversationTop(mConversation, isTop);
    }


    @Override
    public void clearChatMessages() {
        mIMClient.conversationManager().clearAllConversationMessages(mConversation);
    }

    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactBean contact) {

        }

        @Override
        public void onContactDeleted(ContactBean contact) {

        }

        @Override
        public void onContactUpdate(final ContactBean contact) {
            if (mContactBean.equals(contact)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContactBean = contact;
                        mContactInfoContractView.updateContactInfo(contact);
                    }
                });
            }
        }
    };

}
