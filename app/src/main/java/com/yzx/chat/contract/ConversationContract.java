package com.yzx.chat.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ConversationBean;

import java.util.List;

/**
 * Created by YZX on 2017年11月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ConversationContract {

    public interface View extends BaseView<Presenter> {
        List<ConversationBean> getOldConversationList();

        void updateListView(DiffUtil.DiffResult diffResult, List<ConversationBean> newConversationList);
    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllConversation(List<ConversationBean> oldConversationList);
    }
}
