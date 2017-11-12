package com.yzx.chat.contract;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatContract {
    public interface View extends BaseView<Presenter> {
        void showNew(EMMessage message);
        void showNew(List<EMMessage> messageList);
        void showMore(List<EMMessage> messageList,boolean isHasMoreMessage);
    }


    public interface Presenter extends BasePresenter<View> {

        void init(String conversationID);
        void reset();
        void sendMessage(String message);
        void loadMoreMessage(String lastMessageID);
        boolean isLoadingMore();
        boolean hasMoreMessage();

    }
}
