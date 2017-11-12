package com.yzx.chat.presenter;

import android.os.Handler;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {

    private static final int MIN_LOAD_SIZE = 20;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private static String mToChatName;
    private LoadMoreTask mLoadMoreTask;

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
    }

    @Override
    public void init(String conversationID) {
        mToChatName = conversationID;
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationID);
        List<EMMessage> messageList = conversation.getAllMessages();
        int count = messageList.size();
        if (count == 0) {
            return;
        }
        if (count < MIN_LOAD_SIZE) {
            List<EMMessage> dbMessageList = conversation.loadMoreMsgFromDB(messageList.get(0).getMsgId(), MIN_LOAD_SIZE - count);
            dbMessageList.addAll(messageList);
            mChatView.showNew(dbMessageList);
        } else {
            mChatView.showNew(messageList);
        }
    }

    @Override
    public void reset() {
        mToChatName = null;
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        mHandler.removeCallbacksAndMessages(null);
        NetworkUtil.cancel(mLoadMoreTask);
    }

    @Override
    public void sendMessage(String content) {
        EMMessage message = EMMessage.createTxtSendMessage(content, mToChatName);
        EMClient.getInstance().chatManager().sendMessage(message);
        mChatView.showNew(message);
    }

    @Override
    public void loadMoreMessage(String startID, int count) {
        NetworkUtil.cancel(mLoadMoreTask);
        mLoadMoreTask = new LoadMoreTask(this, mToChatName, startID, count);
        mLoadMoreTask.execute();
    }

    private void loadMoreComplete(List<EMMessage> messageList) {
        mChatView.showMore(messageList);
    }

    private void loadNewComplete(List<EMMessage> messageList) {
        mChatView.showNew(messageList);
    }

    private final EMMessageListener mMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            final List<EMMessage> messageList = new LinkedList<>();
            for (EMMessage message : messages) {
                if (mToChatName.equals(message.conversationId())) {
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
        return mToChatName;
    }

    private static class LoadMoreTask extends NetworkAsyncTask<Void, List<EMMessage>> {
        private String mConversationID;
        private String mStartID;
        private int mCount;

        LoadMoreTask(Object lifeCycleDependence, String conversationID,
                     String startID, int count) {
            super(lifeCycleDependence);
            mConversationID = conversationID;
            mStartID = startID;
            mCount = count;
        }


        @Override
        protected List<EMMessage> doInBackground(Void... voids) {
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mConversationID);
            return conversation.loadMoreMsgFromDB(mStartID, mCount);
        }

        @Override
        protected void onPostExecute(List<EMMessage> emMessages, Object lifeCycleObject) {
            super.onPostExecute(emMessages, lifeCycleObject);
            ChatPresenter presenter = (ChatPresenter) lifeCycleObject;
            presenter.loadMoreComplete(emMessages);
        }
    }
}
