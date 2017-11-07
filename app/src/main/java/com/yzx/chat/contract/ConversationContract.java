package com.yzx.chat.contract;

import com.hyphenate.chat.EMConversation;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationContract {

    public interface View extends BaseView<Presenter> {
        void updateListView(List<EMConversation> conversationList);
    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllConversation();

    }
}
