package com.yzx.chat.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ConversationBean;

import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationContract {

    public interface View extends BaseView<Presenter> {

        void updateConversationListView(DiffUtil.DiffResult diffResult, List<Conversation> conversationList);

        void removeConversationItem(Conversation conversation);

        void enableDisconnectionHint(boolean isEnable);

    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllConversations();

        void setConversationToTop(Conversation conversation, boolean isTop);

        void removeConversation(Conversation conversation);

        void clearChatMessages(Conversation conversation);

        boolean isConnected();
    }
}
