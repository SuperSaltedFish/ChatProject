package com.yzx.chat.presenter;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.util.LogUtil;

import java.io.File;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {

    public static String sConversationID;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private IMClient mIMClient;

    private Conversation mConversation;
    private boolean mHasMoreMessage;
    private boolean mIsLoadingMore;


    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
    }

    @Override
    public void detachView() {
        mIMClient.chatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mIMClient.chatManager().removeOnMessageSendStateChangeListener(mOnMessageSendStateChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mChatView = null;
        mHandler = null;
        sConversationID = null;
    }

    @Override
    public void init(Conversation conversation) {
        mConversation = conversation;
        sConversationID = mConversation.getTargetId();
        mHasMoreMessage = true;
        mIsLoadingMore = false;
        mIMClient.chatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, sConversationID);
        mIMClient.chatManager().addOnMessageSendStateChangeListener(mOnMessageSendStateChangeListener, sConversationID);
        if (mConversation.getUnreadMessageCount() != 0) {
            mIMClient.conversationManager().clearConversationUnreadStatus(mConversation);
        }
        List<Message> messageList = mIMClient.chatManager().getHistoryMessages(mConversation, -1, Constants.CHAT_MESSAGE_PAGE_SIZE);
        if (messageList == null || messageList.size() == 0) {
            mHasMoreMessage = false;
            return;
        }

        if (messageList.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            mHasMoreMessage = false;
        }
        mChatView.addNewMessage(messageList);
    }

    @Override
    public void resendMessage(Message message) {
        sendMessage(message);
    }


    @Override
    public void sendTextMessage(String content) {
        sendMessage(TextMessage.obtain(content));
    }

    @Override
    public void sendVoiceMessage(String filePath, int timeLengthSec) {
        sendMessage(VoiceMessage.obtain(Uri.fromFile(new File(filePath)), timeLengthSec));
    }

    @Override
    public void loadMoreMessage(int lastMessageID) {
        mIsLoadingMore = true;
        mIMClient.chatManager().asyncGetHistoryMessages(
                mConversation,
                lastMessageID,
                Constants.CHAT_MESSAGE_PAGE_SIZE,
                new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        if (messages == null || messages.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
                            mHasMoreMessage = false;
                        }
                        loadMoreComplete(messages);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        LogUtil.e(errorCode.getMessage());
                        loadMoreComplete(null);
                    }
                });
    }

    @Override
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasMoreMessage() {
        return mHasMoreMessage;
    }

    @Override
    public void setVoiceMessageAsListened(Message message) {
        mIMClient.chatManager().setVoiceMessageAsListened(message);
    }

    @Override
    public void saveMessageDraft(String draft) {
        String oldDraft = mConversation.getDraft();
        if ((TextUtils.isEmpty(draft)&&TextUtils.isEmpty(oldDraft))||draft.equals(oldDraft)) {
            return;
        }
        mIMClient.conversationManager().saveConversationDraft(mConversation, draft);
    }

    private void sendMessage(MessageContent messageContent) {
        sendMessage(Message.obtain(mConversation.getTargetId(), Conversation.ConversationType.PRIVATE, messageContent));
    }

    private void sendMessage(Message message) {
        mIMClient.chatManager().sendMessage(message);
    }


    private void loadNewComplete(final Message message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatView.addNewMessage(message);
            }
        });
    }

    private void loadMoreComplete(final List<Message> messages) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsLoadingMore = false;
                mChatView.addMoreMessage(messages, mHasMoreMessage);
            }
        });
    }


    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {

        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            loadNewComplete(message);
        }
    };

    private final ChatManager.OnMessageSendStateChangeListener mOnMessageSendStateChangeListener = new ChatManager.OnMessageSendStateChangeListener() {
        @Override
        public void onSendProgress(Message message) {
            mChatView.addNewMessage(message);
        }

        @Override
        public void onSendSuccess(Message message) {
            mChatView.updateMessage(message);
        }

        @Override
        public void onSendFail(Message message) {
            mChatView.updateMessage(message);
        }
    };

}
