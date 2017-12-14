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
        void showNewMessage(EMMessage message);

        void showNewMessage(List<EMMessage> messageList);

        void showMoreMessage(List<EMMessage> messageList, boolean isHasMoreMessage);

        void updateMessageState(int position);

        List<EMMessage> getAllMessage();
    }


    public interface Presenter extends BasePresenter<View> {

        void init(String conversationID);

        void reset();

        void sendMessage(String message);

        void sendVoiceRecorder(String filePath,int timeLength);

        void loadMoreMessage(String lastMessageID);

        boolean isLoadingMore();

        boolean hasMoreMessage();

    }
}
