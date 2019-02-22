package com.yzx.chat.module.conversation.presenter;


import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ChatManager;
import com.yzx.chat.core.ConversationManager;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.conversation.contract.ConversationContract;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationPresenter implements ConversationContract.Presenter {

    private ConversationContract.View mConversationView;

    private AppClient mAppClient;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.addConnectionListener(mOnConnectionStateChangeListener);
        mAppClient.getChatManager().addOnMessageSendStateChangeListener(mOnMessageSendListener, null);
        mAppClient.getConversationManager().addConversationStateChangeListener(mOnConversationChangeListener);
    }

    @Override
    public void detachView() {
        mAppClient.removeConnectionListener(mOnConnectionStateChangeListener);
        mAppClient.getChatManager().removeOnMessageSendStateChangeListener(mOnMessageSendListener);
        mAppClient.getConversationManager().removeConversationStateChangeListener(mOnConversationChangeListener);
        mConversationView = null;
    }

    @Override
    public void refreshAllConversations() {
        mAppClient.getConversationManager().getAllConversations(new LifecycleMVPResultCallback<List<Conversation>>(mConversationView, false) {
            @Override
            protected void onSuccess(List<Conversation> result) {
                if (result == null) {
                    result = new ArrayList<>(0);
                }
                mConversationView.showConversationList(result);
            }

            @Override
            protected boolean onError(int code, String error) {
                LogUtil.e("Conversation refresh error: " + error);
                return true;
            }
        });
    }

    @Override
    public void setConversationTop(Conversation conversation, boolean isTop) {
        mAppClient.getConversationManager().setTopConversation(conversation.getConversationType(), conversation.getTargetId(), isTop, null);
    }


    @Override
    public void deleteConversation(Conversation conversation) {
        mAppClient.getConversationManager().removeConversation(conversation.getConversationType(), conversation.getTargetId(), null);
    }

    @Override
    public void clearConversationMessages(Conversation conversation) {
        mAppClient.getConversationManager().clearConversationMessages(conversation.getConversationType(), conversation.getTargetId(), null);
    }

    @Override
    public boolean isConnectedToServer() {
        return mAppClient.isConnected();
    }


    private final AppClient.OnConnectionStateChangeListener mOnConnectionStateChangeListener = new AppClient.OnConnectionStateChangeListener() {
        @Override
        public void onConnected() {
            mConversationView.setEnableDisconnectionHint(false);
        }

        @Override
        public void onDisconnected() {
            mConversationView.setEnableDisconnectionHint(true);
        }

        @Override
        public void onUserInvalid() {

        }
    };


    private final ConversationManager.OnConversationChangeListener mOnConversationChangeListener = new ConversationManager.OnConversationChangeListener() {
        @Override
        public void onConversationChange(final Conversation conversation, int typeCode) {
            LogUtil.e("Conversation change,code: " + typeCode);
            refreshAllConversations();
        }
    };

    private final ChatManager.OnMessageSendListener mOnMessageSendListener = new ChatManager.OnMessageSendListener() {
        @Override
        public void onAttached(Message message) {

        }

        @Override
        public void onProgress(Message message, int progress) {

        }

        @Override
        public void onSuccess(Message message) {
            if (mConversationView.isForeground()) {
                refreshAllConversations();
            }
        }

        @Override
        public void onError(Message message) {
            if (mConversationView.isForeground()) {
                refreshAllConversations();
            }
        }

        @Override
        public void onCanceled(Message message) {

        }
    };
}
