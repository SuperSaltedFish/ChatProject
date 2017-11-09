package com.yzx.chat.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2017年06月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationBean {


    public static final int SINGLE = 0;
    public static final int GROUP = 1;

    private int mConversationType;

    private String mName;
    private String mConversationID;
    private String mLastMsgContent;
    private long mLastMsgTime;
    private int mUnreadMsgCount;

    public ConversationBean(@ConversationMode int conversationType) {
        mConversationType = conversationType;

    }

    @IntDef({SINGLE, GROUP})
    @Retention(RetentionPolicy.SOURCE)
    @interface ConversationMode {
    }

    public int getConversationType() {
        return mConversationType;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getConversationID() {
        return mConversationID;
    }

    public void setConversationID(String conversationID) {
        mConversationID = conversationID;
    }

    public String getLastMsgContent() {
        return mLastMsgContent;
    }

    public void setLastMsgContent(String lastMsgContent) {
        mLastMsgContent = lastMsgContent;
    }

    public long getLastMsgTime() {
        return mLastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        mLastMsgTime = lastMsgTime;
    }

    public int getUnreadMsgCount() {
        return mUnreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        mUnreadMsgCount = unreadMsgCount;
    }

    public static class Single extends ConversationBean {

        public Single() {
            super(SINGLE);
        }

    }

    public static class Group extends ConversationBean {

        public Group() {
            super(GROUP);
        }

    }
}
