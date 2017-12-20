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

        void updateListView(DiffUtil.DiffResult diffResult, List<Conversation> newConversationList);
        void updateListViewByPosition(int position,Conversation newBean);
    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllConversations();

        void refreshSingleConversation(Conversation conversation);
    }
}
