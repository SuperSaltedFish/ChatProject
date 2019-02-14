package com.yzx.chat.core.entity;

import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Created by YZX on 2017年06月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ConversationEntity {


    public static final int SINGLE = 0;
    public static final int GROUP = 1;

    private int mConversationType;

    private String mNickname;
    private String mRemarkName;
    private String mUserID;
    private String mConversationID;
    private String mLastMsgContent;
    private long mLastMsgTime;
    private int mUnreadMsgCount;

    public ConversationEntity(@ConversationMode int conversationType) {
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
        if (TextUtils.isEmpty(mRemarkName)) {
            return mNickname;
        } else {
            return mRemarkName;
        }
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public String getRemarkName() {
        return mRemarkName;
    }

    public void setRemarkName(String remarkName) {
        mRemarkName = remarkName;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
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

    public static class Single extends ConversationEntity {

        public Single() {
            super(SINGLE);
        }

    }

    public static class Group extends ConversationEntity {

        public Group() {
            super(GROUP);
        }

    }
}
