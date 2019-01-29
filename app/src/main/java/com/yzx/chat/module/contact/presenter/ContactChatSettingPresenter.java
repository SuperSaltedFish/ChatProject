package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactChatSettingContract;
import com.yzx.chat.core.manager.ContactManager;
import com.yzx.chat.core.IMClient;
import com.yzx.chat.core.listener.ResultCallback;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactChatSettingPresenter implements ContactChatSettingContract.Presenter {

    private ContactChatSettingContract.View mContactChatSettingView;
    private IMClient mIMClient;
    private Conversation mConversation;
    private String mContactID;

    @Override
    public void attachView(ContactChatSettingContract.View view) {
        mContactChatSettingView = view;
        mIMClient = IMClient.getInstance();
        mIMClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mContactChatSettingView = null;
    }

    @Override
    public void init(String contactID) {
        mContactID = contactID;
        mContactChatSettingView.updateContactInfo(mIMClient.getContactManager().getContact(contactID).getRemark());
        mConversation = mIMClient.getConversationManager().getConversation(Conversation.ConversationType.PRIVATE, contactID);
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(contactID);
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.PRIVATE);
        }

        mContactChatSettingView.switchTopState(mConversation.isTop());
        mIMClient.getConversationManager().isEnableConversationNotification(mConversation, new ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus result) {
                mContactChatSettingView.switchRemindState(result == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    @Override
    public ContactEntity getContact() {
        return mIMClient.getContactManager().getContact(mContactID);
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mIMClient.getConversationManager().setEnableConversationNotification(mConversation.getConversationType(), mConversation.getTargetId(), isEnable);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mIMClient.getConversationManager().setConversationTop(mConversation.getConversationType(), mConversation.getTargetId(), isTop);
    }


    @Override
    public void clearChatMessages() {
        mIMClient.getConversationManager().clearAllConversationMessages(mConversation.getConversationType(), mConversation.getTargetId());
    }

    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactEntity contact) {

        }

        @Override
        public void onContactDeleted(ContactEntity contact) {

        }

        @Override
        public void onContactUpdate(ContactEntity contact) {
            if (mContactID.equals(contact.getUserProfile().getUserID())) {
                mContactChatSettingView.updateContactInfo(contact.getRemark());
            }
        }
    };

}
