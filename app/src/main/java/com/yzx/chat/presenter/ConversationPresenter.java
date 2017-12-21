package com.yzx.chat.presenter;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationPresenter implements ConversationContract.Presenter {

    private ConversationContract.View mConversationView;

    private RefreshAllConversationTask mRefreshTask;
    private List<Conversation> mConversationList;
    private ChatClientManager mChatManager;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mConversationList = new ArrayList<>(64);
        mChatManager = ChatClientManager.getInstance();
        mChatManager.addConnectionListener(mOnConnectionStateChangeListener);
        mChatManager.addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
    }

    @Override
    public void detachView() {
        mChatManager.removeConnectionListener(mOnConnectionStateChangeListener);
        mChatManager.removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mConversationList.clear();
        mConversationList = null;
        mConversationView = null;
        NetworkUtil.cancelTask(mRefreshTask);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllConversations() {
        NetworkUtil.cancelTask(mRefreshTask);
        mRefreshTask = new RefreshAllConversationTask(this);
        mRefreshTask.execute(mConversationList);
    }

    @Override
    public void refreshSingleConversation(Conversation conversation) {
        conversation = mChatManager.getConversation(conversation.getConversationType(), conversation.getTargetId());
        if (conversation != null) {
            synchronized (ConversationPresenter.class) {
                String conversationID = conversation.getTargetId();
                Conversation temp;
                for (int i = 0, size = mConversationList.size(); i < size; i++) {
                    temp = mConversationList.get(i);
                    if (temp.getTargetId().equals(conversationID)) {
                        mConversationList.set(i, conversation);
                        mConversationView.updateListViewByPosition(i, conversation);
                        break;
                    }
                }
            }
        }
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

    private final ChatClientManager.onConnectionStateChangeListener mOnConnectionStateChangeListener = new ChatClientManager.onConnectionStateChangeListener() {

        @Override
        public void onConnected() {
            refreshAllConversations();
        }

        @Override
        public void onDisconnected(String reason) {

        }
    };

    private final ChatClientManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatClientManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            if (untreatedCount == 0) {
                refreshAllConversations();
            }
        }
    };


    private static class RefreshAllConversationTask extends NetworkAsyncTask<ConversationPresenter, List<Conversation>, DiffUtil.DiffResult> {

        RefreshAllConversationTask(ConversationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<Conversation>[] oldConversation) {
            synchronized (ConversationPresenter.class) {
                ChatClientManager chatManager = ChatClientManager.getInstance();
                List<Conversation> oldConversationList = oldConversation[0];
                List<Conversation> newConversationList = chatManager.getAllConversations(Conversation.ConversationType.PRIVATE);
                if (newConversationList == null) {
                    return null;
                }
                for (Conversation conversation : newConversationList) {
                    if (conversation.getTargetId().equals(ChatPresenter.sConversationID)) {
                        chatManager.clearConversationUnreadStatus(conversation.getConversationType(), conversation.getTargetId());
                        conversation.setUnreadMessageCount(0);
                    }
                }

                //  sortConversationByLastChatTime(filterConversation);

                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<Conversation>(oldConversationList, newConversationList) {
                    @Override
                    public boolean isItemEquals(Conversation oldItem, Conversation newItem) {
                        return oldItem.getTargetId().equals(newItem.getTargetId());
                    }

                    @Override
                    public boolean isContentsEquals(Conversation oldItem, Conversation newItem) {
                        if (oldItem.getSentTime() != newItem.getSentTime()) {
                            return false;
                        }
                        if (oldItem.getUnreadMessageCount() != newItem.getUnreadMessageCount()) {
                            return false;
                        }
                        return true;
                    }
                }, true);
                oldConversationList.clear();
                oldConversationList.addAll(newConversationList);
                return diffResult;
            }

        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ConversationPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            if (diffResult != null) {
                lifeDependentObject.refreshComplete(diffResult);
            }
        }
    }

}
