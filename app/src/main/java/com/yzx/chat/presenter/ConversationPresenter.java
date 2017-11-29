package com.yzx.chat.presenter;


import android.support.v7.util.DiffUtil;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.yzx.chat.base.DiffCalculate;
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
    private ChatClientManager mChatManager;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mConversationList = new ArrayList<>(64);
        mChatManager = ChatClientManager.getInstance();
        mChatManager.addMessageListener(mMessageListener, null);
        mChatManager.addUnreadCountChangeListener(mUnreadChangeListener);
    }

    @Override
    public void detachView() {
        mChatManager.removeMessageListener(mMessageListener);
        mChatManager.removeUnreadCountChangeListener(mUnreadChangeListener);
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

    private final ChatClientManager.UnreadCountChangeListener mUnreadChangeListener = new ChatClientManager.UnreadCountChangeListener() {
        @Override
        public void onMessageUnreadCountChange(int unreadCount) {
            refreshAllConversation(mConversationView.getOldConversationList());
        }

        @Override
        public void onContactUnreadCountChange(int unreadCount) {

        }
    };

    private static class RefreshAllConversationTask extends NetworkAsyncTask<ConversationPresenter, List<ConversationBean>, DiffUtil.DiffResult> {

        RefreshAllConversationTask(ConversationPresenter lifeCycleDependence) {
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
                    } else {
                        unreadCount += conversation.getUnreadMsgCount();
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

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<ConversationBean>(lists[1], filterConversation) {
                @Override
                public boolean isItemEquals(ConversationBean oldItem, ConversationBean newItem) {
                    return oldItem.getConversationID().equals(newItem.getConversationID());
                }

                @Override
                public boolean isContentsEquals(ConversationBean oldItem, ConversationBean newItem) {
                    if (oldItem.getLastMsgTime() != newItem.getLastMsgTime()) {
                        return false;
                    }
                    if (oldItem.getUnreadMsgCount() != newItem.getUnreadMsgCount()) {
                        return false;
                    }
                    return true;
                }
            }, true);

            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ConversationPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }

}
