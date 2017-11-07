package com.yzx.chat.presenter;


import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.network.chat.NetworkAsyncTask;

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

    ConversationContract.View mConversationView;

    private RefreshAllConversationTask mRefreshTask;

    @Override
    public void attachView(ConversationContract.View view) {
        mConversationView = view;
    }

    @Override
    public void detachView() {
        mConversationView = null;
        if (mRefreshTask != null) {
            mRefreshTask.cancel();
        }
    }

    @Override
    public void refreshAllConversation() {
        if (mRefreshTask != null) {
            mRefreshTask.cancel();
        }
        mRefreshTask = new RefreshAllConversationTask(this);
        mRefreshTask.execute();
    }

    private static void sortConversationByLastChatTime(List<EMConversation> conversationList) {
        Collections.sort(conversationList, new Comparator<EMConversation>() {
            @Override
            public int compare(EMConversation o1, EMConversation o2) {
                return (int) (o2.getLastMessage().getMsgTime() - o1.getLastMessage().getMsgTime());
            }
        });
    }

    private static class RefreshAllConversationTask extends NetworkAsyncTask<Void, List<EMConversation>> {

        RefreshAllConversationTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected List<EMConversation> doInBackground(Void... voids) {
            Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
            Collection<EMConversation> allConversations = conversations.values();
            List<EMConversation> filterConversation = new ArrayList<>(allConversations.size());
            for (EMConversation conversation : allConversations) {
                if (conversation.getAllMessages().size() != 0) {



                    filterConversation.add(conversation);
                }
            }
            sortConversationByLastChatTime(filterConversation);
            return filterConversation;
        }

        @Override
        protected void onPostExecute(List<EMConversation> conversations, Object lifeCycleObject) {
            super.onPostExecute(conversations, lifeCycleObject);
            if (conversations.size() == 0) {
                return;
            }
            ConversationPresenter presenter = (ConversationPresenter) lifeCycleObject;
            presenter.mConversationView.updateListView(conversations);
        }
    }
}
