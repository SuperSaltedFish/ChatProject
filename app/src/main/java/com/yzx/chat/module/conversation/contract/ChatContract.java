package com.yzx.chat.module.conversation.contract;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;

import java.util.List;

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ChatContract {
    public interface View extends BaseView<Presenter> {
        void showChatTitle(String title);

        void addNewMessage(Message message);

        void addNewMessage(List<Message> messageList);

        void addMoreMessage(List<Message> messageList, boolean isHasMoreMessage);

        void updateMessage(Message message);

        void clearMessage();

        void enableLoadMoreHint(boolean isEnable);

        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {

        ContactEntity initPrivateChat(String conversationID);

        GroupEntity initGroupChat(String conversationID);

        String getConversationID();

        void resendMessage(Message message);

        void sendTextMessage(String message);

        void sendVoiceMessage(String filePath, long timeLengthMs);

        void sendImageMessage(String imagePath, boolean isOriginal);

        void sendLocationMessage(PoiItem poi);

        void sendVideoMessage(String filePath);

        void sendFileMessage(String filePath);

        void loadMoreMessage(int lastMessageID);

        boolean isLoadingMore();

        boolean hasMoreMessage();

        void setVoiceMessageAsListened(Message message);

        void saveMessageDraft(String draft);

        String getMessageDraft();

    }
}
