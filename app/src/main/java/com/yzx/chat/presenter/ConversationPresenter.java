package com.yzx.chat.presenter;


import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.yzx.chat.base.DiffCalculate;

import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

import java.util.ArrayList;


import java.util.List;

import io.rong.imlib.RongIMClient;
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
    private ChatClientManager mChatManager;
    private Handler mHandler;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
        mConversationList = new ArrayList<>(64);
        mHandler = new Handler();
        mChatManager = ChatClientManager.getInstance();
        mChatManager.addConnectionListener(mOnConnectionStateChangeListener);
        mChatManager.addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
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
    public void setConversationToTop(Conversation conversation, boolean isTop) {
        mChatManager.asyncSetConversationTop(conversation, isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    refreshAllConversations();
                } else {
                    LogUtil.e("setConversationTop  fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }


    @Override
    public void removeConversation(final int position, final Conversation conversation) {
        mChatManager.asyncRemoveConversation(conversation, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mConversationList.size() > position && mConversationList.get(position).getTargetId().equals(conversation.getTargetId())) {
                                mConversationList.remove(position);
                                mConversationView.removeConversationItem(position, conversation);
                            } else {
                                for (int i = 0, size = mConversationList.size(); i < size; i++) {
                                    if (mConversationList.get(i).getTargetId().equals(conversation.getTargetId())) {
                                        mConversationList.remove(i);
                                        mConversationView.removeConversationItem(i, conversation);
                                        return;
                                    }
                                }
                                LogUtil.e("delete conversation fail from presenter");
                            }
                        }
                    });
                } else {
                    LogUtil.e("delete conversation fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    @Override
    public void clearChatMessages(Conversation conversation) {
        mChatManager.asyncDeleteChatMessages(conversation, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                refreshAllConversations();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }


    private void refreshComplete(DiffUtil.DiffResult diffResult) {
        mConversationView.updateConversationListView(diffResult, mConversationList);
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
            ChatClientManager chatManager = ChatClientManager.getInstance();
            List<Conversation> oldConversationList = oldConversation[0];
            List<Conversation> newConversationList = chatManager.getAllConversations(Conversation.ConversationType.PRIVATE);
            if (newConversationList == null) {
                return null;
            }
            for (Conversation conversation : newConversationList) {
                if (conversation.getTargetId().equals(ChatPresenter.sConversationID)) {
                    chatManager.clearConversationUnreadStatus(conversation);
                    conversation.setUnreadMessageCount(0);
                }
            }

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
                    if (oldItem.getLatestMessageId() != newItem.getLatestMessageId()) {
                        return false;
                    }
                    String oldDraft = oldItem.getDraft();
                    String newDraft = newItem.getDraft();
                    if((TextUtils.isEmpty(oldDraft)&&!TextUtils.isEmpty(newDraft))||(!TextUtils.isEmpty(oldDraft)&&TextUtils.isEmpty(newDraft))){
                        return false;
                    }
                    if((!TextUtils.isEmpty(oldDraft)&&!TextUtils.isEmpty(newDraft))&&!oldDraft.equals(newDraft)){
                        return false;
                    }
                    return true;
                }
            }, true);
            oldConversationList.clear();
            oldConversationList.addAll(newConversationList);
            return diffResult;
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
