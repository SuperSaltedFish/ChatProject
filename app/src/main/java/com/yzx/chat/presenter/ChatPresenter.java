package com.yzx.chat.presenter;

import android.os.Handler;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.NetworkUtil;

import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {

    public static String sConversationID;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private LoadMoreTask mLoadMoreTask;
    private ChatClientManager mChatManager;

    private Conversation mConversation;
    private boolean mHasMoreMessage;
    private boolean mIsLoadingMore;


    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
        mChatManager = ChatClientManager.getInstance();
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelTask(mLoadMoreTask);
        mChatManager.removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mChatManager.removeOnMessageSendStateChangeListener(mSendStateChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mChatView = null;
        mHandler = null;
        sToChatName = null;
    }

    @Override
    public void init(Conversation conversation) {
        mConversation = conversation;
        sConversationID = mConversation.getTargetId();
        mHasMoreMessage = true;
        mIsLoadingMore = false;
        mChatManager.addOnMessageReceiveListener(mOnChatMessageReceiveListener, sConversationID);
        mChatManager.addOnMessageSendStateChangeListener(mSendStateChangeListener, sConversationID);
        mChatManager.clearConversationUnreadStatus(mConversation.getConversationType(), mConversation.getTargetId());
        List<Message> messageList = mChatManager.getHistoryMessages(mConversation.getConversationType(), mConversation.getTargetId(), -1, Constants.CHAT_MESSAGE_PAGE_SIZE);
        if (messageList == null || messageList.size() == 0) {
            mHasMoreMessage = false;
            return;
        }

        if (messageList.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            mHasMoreMessage = false;
        }
        mChatView.showNewMessage(messageList);
    }

    @Override
    public EMMessage resendMessage(String messageID) {
        return mChatManager.resendMessage(messageID);
    }


    @Override
    public void sendTextMessage(String content) {
        sendMessage(EMMessage.createTxtSendMessage(content, sToChatName));
    }

    @Override
    public void sendVoiceMessage(String filePath, int timeLength) {
        sendMessage(EMMessage.createVoiceSendMessage(filePath, timeLength, sToChatName));
    }

    @Override
    public void loadMoreMessage(String lastMessageID) {
        mIsLoadingMore = true;
        NetworkUtil.cancelTask(mLoadMoreTask);
        mLoadMoreTask = new LoadMoreTask(this, sToChatName, lastMessageID, Constants.CHAT_MESSAGE_PAGE_SIZE);
        mLoadMoreTask.execute();
    }

    @Override
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasMoreMessage() {
        return mHasMoreMessage;
    }

    private void sendMessage(EMMessage message) {
        mChatManager.sendMessage(message);
        mChatView.showNewMessage(message);
    }

    private void updateMessage(EMMessage message) {
        List<EMMessage> messageList = mChatView.getAllMessage();
        for (int position = 0, length = messageList.size(); position < length; position++) {
            if (messageList.get(position).getMsgId().equals(message.getMsgId())) {
                final int finalPosition = position;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mChatView.updateMessageState(finalPosition);
                    }
                });
                break;
            }
        }
    }

    private void loadMoreComplete(List<EMMessage> messageList) {
        mIsLoadingMore = false;
        if (messageList == null || messageList.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            mHasMoreMessage = false;
        }
        mChatView.showMoreMessage(messageList, mHasMoreMessage);
    }

    private void loadNewComplete(List<EMMessage> messageList) {
        mChatView.showNewMessage(messageList);
    }


    private final ChatClientManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatClientManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(final List<EMMessage> messages) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadNewComplete(messages);
                }
            });
        }
    };

    private final ChatClientManager.OnMessageSendStateChangeListener mSendStateChangeListener = new ChatClientManager.OnMessageSendStateChangeListener() {
        @Override
        public void onSendProgress(EMMessage message, int progress) {

        }

        @Override
        public void onSendSuccess(EMMessage message) {
            updateMessage(message);
        }

        @Override
        public void onSendFail(EMMessage message) {
            updateMessage(message);
        }
    };


    private static class LoadMoreTask extends NetworkAsyncTask<ChatPresenter, Void, List<EMMessage>> {
        private String mConversationID;
        private String mStartID;
        private int mCount;

        LoadMoreTask(ChatPresenter lifeCycleDependence, String conversationID,
                     String startID, int count) {
            super(lifeCycleDependence);
            mConversationID = conversationID;
            mStartID = startID;
            mCount = count;
        }


        @Override
        protected List<EMMessage> doInBackground(Void... voids) {
            return ChatClientManager.getInstance().loadMoreMessage(mConversationID, mStartID, mCount);
        }

        @Override
        protected void onPostExecute(List<EMMessage> emMessages, ChatPresenter lifeDependentObject) {
            super.onPostExecute(emMessages, lifeDependentObject);
            lifeDependentObject.loadMoreComplete(emMessages);
        }
    }
}
