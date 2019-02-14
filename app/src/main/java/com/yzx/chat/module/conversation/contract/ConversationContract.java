package com.yzx.chat.module.conversation.contract;


import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ConversationContract {

    public interface View extends BaseView<Presenter> {

        void updateConversationsFromUI(DiffUtil.DiffResult diffResult, List<Conversation> conversationList);

        void removeConversationFromUI(Conversation conversation);

        void setEnableDisconnectionHint(boolean isEnable);

    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllConversationsIfNeed();

        void refreshAllConversations();

        void setConversationTop(Conversation conversation, boolean isTop);

        void deleteConversation(Conversation conversation);

        void clearConversationMessages(Conversation conversation);

        boolean isConnectedToServer();
    }
}
