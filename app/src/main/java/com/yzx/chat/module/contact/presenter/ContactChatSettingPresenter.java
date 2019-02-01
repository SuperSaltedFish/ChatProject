package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactChatSettingContract;
import com.yzx.chat.core.manager.ContactManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.listener.ResultCallback;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactChatSettingPresenter implements ContactChatSettingContract.Presenter {

    private ContactChatSettingContract.View mContactChatSettingView;
    private AppClient mAppClient;
    private Conversation mConversation;
    private String mContactID;

    @Override
    public void attachView(ContactChatSettingContract.View view) {
        mContactChatSettingView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mAppClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mContactChatSettingView = null;
    }

    @Override
    public void init(String contactID) {
        mContactID = contactID;
        mContactChatSettingView.updateContactInfo(mAppClient.getContactManager().getContact(contactID).getRemark());
        mConversation = mAppClient.getConversationManager().getConversation(Conversation.ConversationType.PRIVATE, contactID);
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(contactID);
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.PRIVATE);
        }

        mContactChatSettingView.switchTopState(mConversation.isTop());
        mAppClient.getConversationManager().isEnableConversationNotification(mConversation, new ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onResult(Conversation.ConversationNotificationStatus result) {
                mContactChatSettingView.switchRemindState(result == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    @Override
    public ContactEntity getContact() {
        return mAppClient.getContactManager().getContact(mContactID);
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mAppClient.getConversationManager().setEnableConversationNotification(mConversation.getConversationType(), mConversation.getTargetId(), isEnable);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mAppClient.getConversationManager().setConversationTop(mConversation.getConversationType(), mConversation.getTargetId(), isTop);
    }


    @Override
    public void clearChatMessages() {
        mAppClient.getConversationManager().clearAllConversationMessages(mConversation.getConversationType(), mConversation.getTargetId());
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
