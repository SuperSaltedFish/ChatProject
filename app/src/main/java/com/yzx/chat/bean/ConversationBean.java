package com.yzx.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2017年06月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationBean implements Parcelable {


    public static final int SINGLE = 0;
    public static final int GROUP = 1;

    private int mConversationMode;

    private String mName;
    private String mLastMessage;
    private String mTime;

    public ConversationBean(@ConversationMode int conversationMode) {
        mConversationMode = conversationMode;

    }

    @IntDef({SINGLE, GROUP})
    @Retention(RetentionPolicy.SOURCE)
    @interface ConversationMode {
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String lastMessage) {
        mLastMessage = lastMessage;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    @ConversationMode
    public int getConversationMode() {
        return mConversationMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mConversationMode);
        dest.writeString(this.mName);
        dest.writeString(this.mLastMessage);
        dest.writeString(this.mTime);
    }

    protected ConversationBean(Parcel in) {
        this.mConversationMode = in.readInt();
        this.mName = in.readString();
        this.mLastMessage = in.readString();
        this.mTime = in.readString();
    }

    public static final Creator<ConversationBean> CREATOR = new Creator<ConversationBean>() {
        @Override
        public ConversationBean createFromParcel(Parcel source) {
            return new ConversationBean(source);
        }

        @Override
        public ConversationBean[] newArray(int size) {
            return new ConversationBean[size];
        }
    };

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
