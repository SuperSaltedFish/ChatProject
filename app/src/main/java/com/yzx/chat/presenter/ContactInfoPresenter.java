package com.yzx.chat.presenter;

import android.os.Looper;

import com.yzx.chat.contract.ContactInfoContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.LogUtil;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年01月21日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactInfoPresenter implements ContactInfoContract.Presenter {

    private ContactInfoContract.View mContactInfoContractView;
    private IMClient mIMClient;
    private Conversation mConversation;

    @Override
    public void attachView(ContactInfoContract.View view) {
        mContactInfoContractView = view;
        mIMClient = IMClient.getInstance();
    }

    @Override
    public void detachView() {
        mContactInfoContractView = null;
        mIMClient = null;
    }


    @Override
    public void init(String userID) {
        mConversation = mIMClient.conversationManager().getConversation(Conversation.ConversationType.PRIVATE, userID);
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(userID);
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

}
