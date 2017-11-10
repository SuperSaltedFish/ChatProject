package com.yzx.chat.presenter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {

    private static final int MIN_LOAD_SIZE = 20;

    private ChatContract.View mChatView;
    private EMConversation mConversation;

    private LoadMoreTask mLoadMoreTask;

    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
    }

    @Override
    public void detachView() {
        mChatView = null;
        NetworkUtil.cancel(mLoadMoreTask);
    }

    @Override
    public void initMessage(String conversationID) {
        mConversation = EMClient.getInstance().chatManager().getConversation(conversationID);
        List<EMMessage> messageList = mConversation.getAllMessages();
        int count = messageList.size();
        if (count < MIN_LOAD_SIZE) {
            List<EMMessage> dbMessageList = mConversation.loadMoreMsgFromDB(mConversation.getLastMessage().getMsgId(), MIN_LOAD_SIZE - count);
            dbMessageList.addAll(messageList);
            mChatView.startShow(dbMessageList);
        }else {
            mChatView.startShow(messageList);
        }

    }

    @Override
    public void loadMoreMessage(String startID, int count) {
        NetworkUtil.cancel(mLoadMoreTask);
        mLoadMoreTask = new LoadMoreTask(this, mConversation, startID, count);
        mLoadMoreTask.execute();
    }

    private void loadMoreComplete(List<EMMessage> messageList) {
        mChatView.showMore(messageList);
    }

    private static class LoadMoreTask extends NetworkAsyncTask<Void, List<EMMessage>> {
        private EMConversation mConversation;
        private String mStartID;
        private int mCount;

        LoadMoreTask(Object lifeCycleDependence, EMConversation conversation,
                     String startID, int count) {
            super(lifeCycleDependence);
            mConversation = conversation;
            mStartID = startID;
            mCount = count;
        }


        @Override
        protected List<EMMessage> doInBackground(Void... voids) {
            return mConversation.loadMoreMsgFromDB(mStartID, mCount);
        }

        @Override
        protected void onPostExecute(List<EMMessage> emMessages, Object lifeCycleObject) {
            super.onPostExecute(emMessages, lifeCycleObject);
            ChatPresenter presenter = (ChatPresenter) lifeCycleObject;
            presenter.loadMoreComplete(emMessages);
        }
    }
}
