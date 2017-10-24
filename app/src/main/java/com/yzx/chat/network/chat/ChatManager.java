package com.yzx.chat.network.chat;

import android.support.annotation.IntDef;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatManager {

    public static final int CHAT_SINGLE = 0;
    public static final int CHAT_GROUP = 1;

    //用 <span></span>@IntDef "包住" 常量；
    // @Retention 定义策略
    // 声明构造器
    @IntDef({CHAT_SINGLE, CHAT_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChatType {
    }

    public void sendMessage(String content, String toChatUsername, @ChatType int messageType) {
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        //如果是群聊，设置chattype，默认是单聊
        if (messageType == CHAT_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        }
        EMClient.getInstance().chatManager().sendMessage(message);
    }


}
