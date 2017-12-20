package com.yzx.chat.bean;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ContactBean {
//
    public static final String CONTACT_TYPE_ADDED = "Added";
    public static final String CONTACT_TYPE_DELETED = "Deleted";
    public static final String CONTACT_TYPE_REQUEST = "Invited";
    public static final String CONTACT_TYPE_ACCEPTED = "Accepted";
    public static final String CONTACT_TYPE_DECLINED = "Declined";
    public static final String CONTACT_TYPE_INITIATE = "Initiate";

    @StringDef({CONTACT_TYPE_ADDED, CONTACT_TYPE_DELETED, CONTACT_TYPE_REQUEST, CONTACT_TYPE_ACCEPTED, CONTACT_TYPE_DECLINED,CONTACT_TYPE_INITIATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ContactType {
    }

    private String userTo;
    private String userFrom;
    private String type;
    private String reason;
    private boolean isRemind;
    private int time;

    public String getUserTo() {
        return userTo;
    }

    public void setUserTo(String userTo) {
        this.userTo = userTo;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    @ContactType
    public String getType() {
        return type;
    }

    public void setType(@ContactType String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isRemind() {
        return isRemind;
    }

    public void setRemind(boolean remind) {
        isRemind = remind;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
