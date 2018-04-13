package com.yzx.chat.mvp.presenter;


import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.mvp.contract.ContactInfoContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;

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
    public void init(String contactID) {
        mContactBean = mIMClient.contactManager().getContact(contactID);
        mContactInfoContractView.updateContactInfo(mContactBean);
        mConversation = mIMClient.conversationManager().getConversation(Conversation.ConversationType.PRIVATE, mContactBean.getUserProfile().getUserID());
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(mContactBean.getUserProfile().getUserID());
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.PRIVATE);
        }

        mContactInfoContractView.switchTopState(mConversation.isTop());
        mIMClient.conversationManager().isEnableConversationNotification(mConversation, new ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus result) {
                mContactInfoContractView.switchRemindState(result == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);

            }

            @Override
            public void onFailure(String error) {

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
