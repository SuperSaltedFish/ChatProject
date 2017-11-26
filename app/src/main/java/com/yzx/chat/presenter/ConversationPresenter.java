package com.yzx.chat.presenter;


import android.support.v7.util.DiffUtil;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationPresenter implements ConversationContract.Presenter {

    private ConversationContract.View mConversationView;

    private RefreshAllConversationTask mRefreshTask;
    private List<ConversationBean> mConversationList;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mConversationList = new ArrayList<>(64);
        ChatClientManager.getInstance().addMessageListener(mMessageListener,null);
    }

    @Override
    public void detachView() {
        ChatClientManager.getInstance().removeMessageListener(mMessageListener);
        mConversationView = null;
        mConversationList = null;
        NetworkUtil.cancelTask(mRefreshTask);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllConversation(List<ConversationBean> oldConversationList) {
        NetworkUtil.cancelTask(mRefreshTask);
        mRefreshTask = new RefreshAllConversationTask(this);
        mRefreshTask.execute(mConversationList, oldConversationList);
    }

    @Override
    public void markConversationAsRead(String conversationID) {
        EMClient.getInstance().chatManager().getConversation(conversationID).markAllMessagesAsRead();
    }

    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mConversationView.updateListView(diffResult, mConversationList);
    }

    private static void sortConversationByLastChatTime(List<ConversationBean> conversationList) {
        Collections.sort(conversationList, new Comparator<ConversationBean>() {
            @Override
            public int compare(ConversationBean o1, ConversationBean o2) {
                return (int) (o2.getLastMsgTime() - o1.getLastMsgTime());
            }
        });
    }

    private final ChatClientManager.MessageListener mMessageListener = new ChatClientManager.MessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            refreshAllConversation(mConversationView.getOldConversationList());
        }
    };

    private static class RefreshAllConversationTask extends NetworkAsyncTask<List<ConversationBean>, DiffUtil.DiffResult> {

        private RefreshAllConversationTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<ConversationBean>[] lists) {
            Map<String, EMConversation> conversations = ChatClientManager.getInstance().getAllConversations();
            Collection<EMConversation> allConversations = conversations.values();
            List<ConversationBean> filterConversation = lists[0];
            filterConversation.clear();
            ConversationBean bean;
            EMMessage lastMessage;
            int unreadCount = 0;
            for (EMConversation conversation : allConversations) {
                if (conversation.getAllMessages().size() != 0) {
                    if (conversation.conversationId().equals(ChatPresenter.getConversationID())) {
                        conversation.markAllMessagesAsRead();
                    }else {
                        unreadCount+=conversation.getUnreadMsgCount();
                    }
                    lastMessage = conversation.getLastMessage();
                    switch (conversation.getType()) {
                        case Chat:
                            bean = new ConversationBean.Single();
                            break;
                        case GroupChat:
                            bean = new ConversationBean.Group();
                            break;
                        default:
                            continue;
                    }
                    bean.setConversationID(conversation.conversationId());
                    bean.setName(conversation.conversationId());
                    bean.setUnreadMsgCount(conversation.getUnreadMsgCount());
                    bean.setLastMsgContent((((EMTextMessageBody) lastMessage.getBody()).getMessage()));
                    bean.setLastMsgTime(lastMessage.getMsgTime());
                    filterConversation.add(bean);
                }
            }
            ChatClientManager.getInstance().setMessageUnreadCount(unreadCount);
            sortConversationByLastChatTime(filterConversation);
            return DiffUtil.calculateDiff(new DiffCallback(lists[1], filterConversation), true);
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, Object lifeCycleObject) {
            super.onPostExecute(diffResult, lifeCycleObject);
            ConversationPresenter presenter = (ConversationPresenter) lifeCycleObject;
            presenter.refreshComplete(diffResult);
        }

    }

    private static class DiffCallback extends DiffUtil.Callback {

        private List<ConversationBean> mNewData;
        private List<ConversationBean> mOldData;

        DiffCallback(List<ConversationBean> oldData, List<ConversationBean> newData) {
            this.mOldData = oldData;
            this.mNewData = newData;
        }

        @Override
        public int getOldListSize() {
            return mOldData == null ? 0 : mOldData.size();
        }

        @Override
        public int getNewListSize() {
            return mNewData == null ? 0 : mNewData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mNewData.get(newItemPosition).getConversationID().equals(mOldData.get(oldItemPosition).getConversationID());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ConversationBean oldBean = mOldData.get(oldItemPosition);
            ConversationBean newBean = mNewData.get(oldItemPosition);
            if (oldBean.getLastMsgTime() != newBean.getLastMsgTime()) {
                return false;
            }
            if (oldBean.getUnreadMsgCount() != newBean.getUnreadMsgCount()) {
                return false;
            }
            return true;
        }
    }

}
