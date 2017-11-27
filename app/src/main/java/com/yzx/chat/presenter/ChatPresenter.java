package com.yzx.chat.presenter;

import android.os.Handler;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.NetworkUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {


    private ChatContract.View mChatView;
    private Handler mHandler;
    private static String sToChatName;
    private LoadMoreTask mLoadMoreTask;
    private boolean mIsLoadingMore;
    private boolean mHasMoreMessage;

    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
    }

    @Override
    public void detachView() {
        reset();
        mChatView = null;
        mHandler = null;
        sToChatName = null;
    }

    @Override
    public void init(String conversationID) {
        sToChatName = conversationID;
        mHasMoreMessage = true;
        mIsLoadingMore = false;
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationID);
        List<EMMessage> messageList = conversation.getAllMessages();
        int count = messageList.size();
        if (count == 0) {
            return;
        }
        if (count < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            List<EMMessage> dbMessageList = conversation.loadMoreMsgFromDB(messageList.get(0).getMsgId(), Constants.CHAT_MESSAGE_PAGE_SIZE - count);
            dbMessageList.addAll(messageList);
            mChatView.showNew(dbMessageList);
        } else {
            mChatView.showNew(messageList);
        }
    }

    @Override
    public void reset() {
        sToChatName = null;
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        mHandler.removeCallbacksAndMessages(null);
        NetworkUtil.cancelTask(mLoadMoreTask);
    }

    @Override
    public void sendMessage(String content) {
        EMMessage message = EMMessage.createTxtSendMessage(content, sToChatName);
        EMClient.getInstance().chatManager().sendMessage(message);
        mChatView.showNew(message);
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

    private void loadMoreComplete(List<EMMessage> messageList) {
        mIsLoadingMore = false;
        if (messageList == null || messageList.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            mHasMoreMessage = false;
        }
        mChatView.showMore(messageList, mHasMoreMessage);
    }

    private void loadNewComplete(List<EMMessage> messageList) {
        mChatView.showNew(messageList);
    }

    private final EMMessageListener mMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            final List<EMMessage> messageList = new LinkedList<>();
            for (EMMessage message : messages) {
                if (sToChatName.equals(message.conversationId())) {
                    messageList.add(message);
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadNewComplete(messageList);
                }
            });
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {

        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {

        }

        @Override
        public void onMessageDelivered(List<EMMessage> messages) {

        }

        @Override
        public void onMessageRecalled(List<EMMessage> messages) {

        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {

        }
    };

    public static String getConversationID() {
        return sToChatName;
    }

    private static class LoadMoreTask extends NetworkAsyncTask<ChatPresenter,Void, List<EMMessage>> {
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
