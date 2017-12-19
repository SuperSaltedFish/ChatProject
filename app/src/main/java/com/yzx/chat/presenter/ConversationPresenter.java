package com.yzx.chat.presenter;


import android.support.v7.util.DiffUtil;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.database.ConversationDao;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.EMMessageUtil;
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
        mChatManager.addConnectionListener(mConnectionListener);
        mChatManager.addOnMessageReceiveListener(mOnMessageReceiveListener, null);
        mChatManager.addUnreadCountChangeListener(mUnreadChangeListener);
    }

    @Override
    public void detachView() {
        mChatManager.removeConnectionListener(mConnectionListener);
        mChatManager.removeOnMessageReceiveListener(mOnMessageReceiveListener);
        mChatManager.removeUnreadCountChangeListener(mUnreadChangeListener);
        mConversationView = null;
        mConversationList = null;
        NetworkUtil.cancelTask(mRefreshTask);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllConversations(List<ConversationBean> oldConversationList) {
        NetworkUtil.cancelTask(mRefreshTask);
        mRefreshTask = new RefreshAllConversationTask(this);
        mRefreshTask.execute(mConversationList, oldConversationList);
    }

    @Override
    public void refreshSingleConversation(String conversationID) {
        EMConversation emConversation = mChatManager.getSingleConversation(conversationID);
        if (emConversation != null) {
            synchronized (ConversationPresenter.class) {
                ConversationBean bean;
                for (int i = 0, size = mConversationList.size(); i < size; i++) {
                    bean = mConversationList.get(i);
                    if (bean.getConversationID().equals(conversationID)) {
                        EMMessage lastMessage = emConversation.getLastMessage();
                        bean.setLastMsgContent(EMMessageUtil.getMessageDigest(lastMessage));
                        bean.setLastMsgTime(lastMessage.getMsgTime());

                        ConversationDao dao = DBManager.getInstance().getConversationDao();
                        dao.replace(bean);
                        bean = dao.loadSingleConversation(bean.getUserID(), conversationID);
                        mConversationList.set(i, bean);
                        mConversationView.updateListViewByPosition(i, bean);
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

    private final ChatClientManager.ConnectionListener mConnectionListener = new ChatClientManager.ConnectionListener() {
        @Override
        public void onConnected() {
            ChatClientManager.getInstance().loadAllConversationsAndGroups();
            refreshAllConversations(mConversationView.getOldConversationList());
        }

        @Override
        public void onDisconnected(int errorCode) {

        }
    };

    private final ChatClientManager.OnMessageReceiveListener mOnMessageReceiveListener = new ChatClientManager.OnMessageReceiveListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            refreshAllConversations(mConversationView.getOldConversationList());
        }
    };

    private final ChatClientManager.UnreadCountChangeListener mUnreadChangeListener = new ChatClientManager.UnreadCountChangeListener() {
        @Override
        public void onMessageUnreadCountChange(int unreadCount) {
            refreshAllConversations(mConversationView.getOldConversationList());
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
            synchronized (ConversationPresenter.class) {
                Map<String, EMConversation> conversations = ChatClientManager.getInstance().getAllConversations();
                Collection<EMConversation> allConversations = conversations.values();
                List<ConversationBean> filterConversation = lists[0];
                filterConversation.clear();
                ConversationBean bean;
                EMMessage lastMessage;
                String userID = IdentityManager.getInstance().getUserID();
                for (EMConversation conversation : allConversations) {
                    if (conversation.conversationId().equals(ChatPresenter.getConversationID())) {
                        conversation.markAllMessagesAsRead();
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
                    bean.setUserID(userID);
                    bean.setConversationID(conversation.conversationId());
                    bean.setUnreadMsgCount(conversation.getUnreadMsgCount());
                    bean.setLastMsgContent(EMMessageUtil.getMessageDigest(lastMessage));
                    bean.setLastMsgTime(lastMessage.getMsgTime());
                    filterConversation.add(bean);
                }
                ConversationDao dao = DBManager.getInstance().getConversationDao();
                dao.replaceAll(filterConversation);
                filterConversation.clear();
                dao.loadAllConversationToList(userID, filterConversation);

                ChatClientManager.getInstance().setMessageUnreadCount(dao.getUnreadMsgCount(userID));
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

        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, ConversationPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.refreshComplete(diffResult);
        }
    }

}
