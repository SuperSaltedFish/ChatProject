package com.yzx.chat.mvp.presenter;


import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.mvp.contract.ConversationContract;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.network.chat.ConversationManager;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ConversationPresenter implements ConversationContract.Presenter {

    private ConversationContract.View mConversationView;

    private RefreshAllConversationTask mRefreshTask;
    private List<Conversation> mConversationList;
    private IMClient mIMClient;
    private Handler mHandler;

    private boolean isOnceSentMessage;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mConversationList = new ArrayList<>(64);
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.addConnectionListener(mOnConnectionStateChangeListener);
        mIMClient.getChatManager().addOnMessageSendStateChangeListener(mOnMessageSendListener, null);
        mIMClient.getChatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
        mIMClient.getConversationManager().addConversationStateChangeListener(mOnConversationStateChangeListener);
        mIMClient.getGroupManager().addGroupChangeListener(mOnGroupOperationListener);
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
        mIMClient.removeConnectionListener(mOnConnectionStateChangeListener);
        mIMClient.getChatManager().removeOnMessageSendStateChangeListener(mOnMessageSendListener);
        mIMClient.getChatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mIMClient.getConversationManager().removeConversationStateChangeListener(mOnConversationStateChangeListener);
        mIMClient.getGroupManager().removeGroupChangeListener(mOnGroupOperationListener);
        mConversationList.clear();
        mConversationList = null;
        mConversationView = null;
        AsyncUtil.cancelTask(mRefreshTask);
    }

    @Override
    public void refreshAllConversationsIfNeed() {
        if (isOnceSentMessage) {
            LogUtil.e("Conversation change,code: OnceSentMessage");
            isOnceSentMessage = false;
            refreshAllConversations();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshAllConversations() {
        LogUtil.e("refreshAllConversations");
        AsyncUtil.cancelTask(mRefreshTask);
        mRefreshTask = new RefreshAllConversationTask(this);
        mRefreshTask.execute(mConversationList);
    }

    @Override
    public void setConversationTop(Conversation conversation, boolean isTop) {
        mIMClient.getConversationManager().setConversationTop(conversation.getConversationType(), conversation.getTargetId(), isTop);
    }


    @Override
    public void deleteConversation(Conversation conversation) {
        mIMClient.getConversationManager().removeConversation(conversation.getConversationType(), conversation.getTargetId());
    }

    @Override
    public void clearConversationMessages(Conversation conversation) {
        mIMClient.getConversationManager().clearAllConversationMessages(conversation.getConversationType(), conversation.getTargetId());
    }

    @Override
    public boolean isConnectedToServer() {
        return mIMClient.isConnected();
    }


    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mConversationView.updateConversationsFromUI(diffResult, mConversationList);
    }

    private final IMClient.OnConnectionStateChangeListener mOnConnectionStateChangeListener = new IMClient.OnConnectionStateChangeListener() {
        private boolean isConnected = true;

        @Override
        public void onConnected() {
            if (!isConnected) {
                isConnected = true;
                refreshAllConversations();
                mConversationView.setEnableDisconnectionHint(false);
            }
        }

        @Override
        public void onDisconnected(String reason) {
            if (isConnected) {
                isConnected = false;
                mConversationView.setEnableDisconnectionHint(true);
            }
        }
    };

    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            if (untreatedCount == 0) {
                refreshAllConversations();
            }
        }
    };


    private final ConversationManager.OnConversationStateChangeListener mOnConversationStateChangeListener = new ConversationManager.OnConversationStateChangeListener() {
        @Override
        public void onConversationStateChange(final Conversation conversation, int typeCode) {
            LogUtil.e("Conversation change,code: " + typeCode);
            switch (typeCode) {
                case ConversationManager.UPDATE_TYPE_REMOVE:
                case ConversationManager.UPDATE_TYPE_CLEAR_UNREAD_STATUS:
                case ConversationManager.UPDATE_TYPE_SAVE_DRAFT:
                case ConversationManager.UPDATE_TYPE_SET_TOP:
                case ConversationManager.UPDATE_TYPE_CLEAR_MESSAGE:
                case ConversationManager.UPDATE_TYPE_NOTIFICATION_CHANGE:
                case ConversationManager.UPDATE_TYPE_UPDATE:
                    refreshAllConversations();
                    break;
            }
        }
    };

    private final ChatManager.OnMessageSendListener mOnMessageSendListener = new ChatManager.OnMessageSendListener() {
        @Override
        public void onAttached(Message message) {
            isOnceSentMessage = true;
        }

        @Override
        public void onProgress(Message message, int progress) {

        }

        @Override
        public void onSuccess(Message message) {

        }

        @Override
        public void onError(Message message) {

        }

        @Override
        public void onCanceled(Message message) {

        }
    };

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupBean group) {
            refreshAllConversations();
        }

        @Override
        public void onQuitGroup(GroupBean group) {
            refreshAllConversations();
        }

        @Override
        public void onBulletinChange(GroupBean group) {
            refreshAllConversations();
        }

        @Override
        public void onNameChange(GroupBean group) {
            refreshAllConversations();
        }

        @Override
        public void onMemberAdded(GroupBean group, String[] newMembersID) {
            refreshAllConversations();
        }

        @Override
        public void onMemberJoin(GroupBean group, String memberID) {
            refreshAllConversations();
        }

        @Override
        public void onMemberQuit(GroupBean group, GroupMemberBean quitMember) {
            refreshAllConversations();
        }

        @Override
        public void onMemberAliasChange(GroupBean group, GroupMemberBean member, String newAlias) {

        }
    };

    private static class RefreshAllConversationTask extends BackstageAsyncTask<ConversationPresenter, List<Conversation>, DiffUtil.DiffResult> {

        RefreshAllConversationTask(ConversationPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<Conversation>[] oldConversation) {
            synchronized (ConversationPresenter.class) {
                IMClient chatManager = IMClient.getInstance();
                List<Conversation> oldConversationList = oldConversation[0];
                List<Conversation> newConversationList = chatManager.getConversationManager().getAllConversations();
                if (newConversationList != null) {
                    String conversationID;
                    Iterator<Conversation> it = newConversationList.iterator();
                    while (it.hasNext()) {
                        Conversation conversation = it.next();
                        conversationID = conversation.getTargetId();
                        if (conversationID.equals(ChatPresenter.sConversationID) && conversation.getUnreadMessageCount() != 0) {
                            chatManager.getConversationManager().clearConversationUnreadStatus(conversation.getConversationType(), conversation.getTargetId());
                            conversation.setUnreadMessageCount(0);
                        }
                        switch (conversation.getConversationType()) {
                            case PRIVATE:
                                ContactBean contactBean = chatManager.getContactManager().getContact(conversationID);
                                if (contactBean != null) {
                                    conversation.setConversationTitle(contactBean.getName());
                                    conversation.setPortraitUrl(contactBean.getUserProfile().getAvatar());
                                } else {
                                    IMClient.getInstance().getConversationManager().removeConversation(conversation.getConversationType(), conversation.getTargetId(), false);
                                    it.remove();
                                }
                                break;
                            case GROUP:
                                GroupBean group = chatManager.getGroupManager().getGroup(conversationID);
                                if (group != null) {
                                    conversation.setConversationTitle(group.getName());
                                    conversation.setPortraitUrl(group.getAvatarUrlFromMembers());
                                } else {
                                    IMClient.getInstance().getConversationManager().removeConversation(conversation.getConversationType(), conversation.getTargetId(), false);
                                    it.remove();
                                }
                                break;
                        }
                    }
                }
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<Conversation>(oldConversationList, newConversationList) {
                    @Override
                    public boolean isItemEquals(Conversation oldItem, Conversation newItem) {
                        return oldItem.getTargetId().equals(newItem.getTargetId());
                    }

                    @Override
                    public boolean isContentsEquals(Conversation oldItem, Conversation newItem) {
                        if (oldItem.getLatestMessageId() != newItem.getLatestMessageId()) {
                            return false;
                        }
                        if (oldItem.getUnreadMessageCount() != newItem.getUnreadMessageCount()) {
                            return false;
                        }
                        if (oldItem.getSentTime() != newItem.getSentTime()) {
                            return false;
                        }
                        if (!oldItem.getConversationTitle().equals(newItem.getConversationTitle())) {
                            return false;
                        }
                        if (!oldItem.getNotificationStatus().equals(newItem.getNotificationStatus())) {
                            return false;
                        }
                        String oldDraft = oldItem.getDraft();
                        String newDraft = newItem.getDraft();
                        if ((TextUtils.isEmpty(oldDraft) && !TextUtils.isEmpty(newDraft)) || (!TextUtils.isEmpty(oldDraft) && TextUtils.isEmpty(newDraft))) {
                            return false;
                        }
                        if ((!TextUtils.isEmpty(oldDraft) && !TextUtils.isEmpty(newDraft)) && !oldDraft.equals(newDraft)) {
                            return false;
                        }
                        return true;
                    }
                }, true);
                oldConversationList.clear();
                if (newConversationList != null && newConversationList.size() > 0) {
                    oldConversationList.addAll(newConversationList);
                }
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
