package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.mvp.contract.ContactProfileContract;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年01月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactProfilePresenter implements ContactProfileContract.Presenter {

    private ContactProfileContract.View mContactProfileView;
    private ContactBean mContactBean;
    private Conversation mConversation;
    private Handler mHandler;
    private IMClient mIMClient;

    @Override
    public void attachView(ContactProfileContract.View view) {
        mContactProfileView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.contactManager().addContactChangeListener(mOnContactChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.contactManager().removeContactChangeListener(mOnContactChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mContactProfileView = null;
    }


    @Override
    public void init(String contactID) {
        mContactBean = mIMClient.contactManager().getContact(contactID);
        if (mContactBean == null) {
            mContactProfileView.goBack();
            return;
        } else {
            mContactProfileView.updateContactInfo(mContactBean);
        }

        mContactProfileView.updateContactInfo(mContactBean);
        mConversation = mIMClient.conversationManager().getConversation(Conversation.ConversationType.PRIVATE, mContactBean.getUserProfile().getUserID());
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(mContactBean.getUserProfile().getUserID());
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.PRIVATE);
        }

        mContactProfileView.switchTopState(mConversation.isTop());
        mIMClient.conversationManager().isEnableConversationNotification(mConversation, new ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus result) {
                mContactProfileView.switchRemindState(result == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    @Override
    public ContactBean getContact() {
        return mContactBean;
    }

    @Override
    public void deleteContact() {
        mContactProfileView.setEnableProgressDialog(true);
        mIMClient.contactManager().deleteContact(mContactBean, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (mContactBean.getUserProfile().getUserID().equals(ChatPresenter.sConversationID)) {
                    mContactProfileView.finishChatActivity();
                }
                mContactProfileView.setEnableProgressDialog(false);
                mContactProfileView.goBack();
            }

            @Override
            public void onFailure(String error) {
                mContactProfileView.setEnableProgressDialog(false);
                mContactProfileView.showError(error);
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
        public void onContactUpdate( ContactBean contact) {
            if (contact.equals(mContactBean)) {
                mContactBean = contact;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContactProfileView.updateContactInfo(mContactBean);
                    }
                });
            }
        }
    };

}
