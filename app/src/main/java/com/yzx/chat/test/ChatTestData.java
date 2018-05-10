package com.yzx.chat.test;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatTestData {

    public static List<ChatBean> getTestData() {
        List<ChatBean> chatList = new ArrayList<>();
        chatList.add(new ChatBean(ChatBean.CHAT_SEND, ChatBean.TYPE_TEXT, "I want to be a good engineer."));
        chatList.add(new ChatBean(ChatBean.CHAT_RECEIVE, ChatBean.TYPE_TEXT, "OK,you wait for notice."));
        chatList.add(new ChatBean(ChatBean.CHAT_SEND, ChatBean.TYPE_TEXT, "will you tell me something about yourself"));
        chatList.add(new ChatBean(ChatBean.CHAT_SEND, ChatBean.TYPE_TEXT, ":status,status is more important than money ,when you have good status ,you will have money"));
        chatList.add(new ChatBean(ChatBean.CHAT_RECEIVE, ChatBean.TYPE_IMAGE, "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496591266827&di=dec4f387a4c0023a6e5cb204da349433&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F16%2F96%2F85%2F77m58PICyBG_1024.jpg"));
        chatList.add(new ChatBean(ChatBean.CHAT_SEND, ChatBean.TYPE_TEXT, "because the workplace have long way to my house. I should spend 2 hours in the way."));
        chatList.add(new ChatBean(ChatBean.CHAT_SEND, ChatBean.TYPE_TEXT, "I want to be a good engineer."));
        chatList.add(new ChatBean(ChatBean.CHAT_RECEIVE, ChatBean.TYPE_TEXT, "OK,you wait for notice."));
        return chatList;

    }

    public static class ChatBean {

        public final static int CHAT_SEND = 10;
        public final static int CHAT_RECEIVE = 20;

        public final static int TYPE_TEXT = 1;
        public final static int TYPE_IMAGE = 2;
        public final static int TYPE_MOVIE = 3;

        private int chatType;
        private int contentType;
        private String content;
        private String headImageUrl;


        @IntDef({TYPE_TEXT, TYPE_IMAGE, TYPE_MOVIE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ContentType {
        }

        @IntDef({CHAT_SEND, CHAT_RECEIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ChatType {
        }

        public ChatBean() {
        }

        public ChatBean(@ChatType int chatType, @ContentType int contentType, String content) {
            this.contentType = contentType;
            this.chatType = chatType;
            this.content = content;
        }

        @ChatType
        public int getChatType() {
            return chatType;
        }

        public void setChatType(@ChatType int chatType) {
            this.chatType = chatType;
        }

        @ContentType
        public int getContentType() {
            return contentType;
        }

        public void setContentType(@ContentType int contentType) {
            this.contentType = contentType;
        }

        public String getHeadImageUrl() {
            return headImageUrl;
        }

        public void setHeadImageUrl(String headImageUrl) {
            this.headImageUrl = headImageUrl;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }




}






