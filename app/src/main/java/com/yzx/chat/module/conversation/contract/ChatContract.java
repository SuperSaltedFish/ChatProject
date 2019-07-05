package com.yzx.chat.module.conversation.contract;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.BasicInfoProvider;

import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ChatContract {
    public interface View extends BaseView<Presenter> {
        void showChatTitle(String title);

        void showNewMessage(Message message);

        void showNewMessage(List<Message> messageList, boolean isHasMoreMessage);

        void showMoreMessage(List<Message> messageList, boolean isHasMoreMessage);

        void refreshMessage(Message message);

        void clearMessage();

        void enableLoadMoreHint(boolean isEnable);

        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {

        BasicInfoProvider init(String conversationID, Conversation.ConversationType type);

        void sendTextMessage(String message);

        void sendVoiceMessage(String filePath, long timeLengthMs);

        void sendImageMessage(String imagePath, boolean isOriginal);

        void sendLocationMessage(PoiItem poi);

        void sendVideoMessage(String filePath);

        void sendFileMessage(String filePath);

        void resendMessage(Message message);

        void loadMoreMessage(int lastMessageID);

        void setVoiceMessageAsListened(Message message);

        void saveMessageDraft(String draft);

        String getMessageDraft();

        void saveKeyBoardHeight(int height);

        int getKeyBoardHeight();

    }
}
