package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatContract {
    public interface View extends BaseView<Presenter> {
        void addNewMessage(Message message);

        void addNewMessage(List<Message> messageList);

        void addMoreMessage(List<Message> messageList, boolean isHasMoreMessage);

        void updateMessage(Message message);

    }


    public interface Presenter extends BasePresenter<View> {

        void init(Conversation conversation);

        void resendMessage(Message message);

        void sendTextMessage(String message);

        void sendVoiceMessage(String filePath, int timeLengthSec);

        void loadMoreMessage(int lastMessageID);

        boolean isLoadingMore();

        boolean hasMoreMessage();

        void setVoiceMessageAsListened(Message message);

        void saveMessageDraft(String draft);

    }
}
