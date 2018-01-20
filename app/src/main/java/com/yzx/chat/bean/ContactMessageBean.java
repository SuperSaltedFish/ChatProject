package com.yzx.chat.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ContactMessageBean {

    public static final int TYPE_REQUESTING = 1;
    public static final int TYPE_ADDED = 2;
    public static final int TYPE_REFUSED = 3;
    public static final int TYPE_VERIFYING = 4;
    public static final int TYPE_DISAGREE = 5;


    @IntDef({TYPE_REQUESTING, TYPE_ADDED, TYPE_REFUSED, TYPE_VERIFYING, TYPE_DISAGREE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    private int indexID;
    private String userTo;
    private String userFrom;
    private String reason;
    private String avatarUrl;
    private String nickname;
    private boolean isRemind;
    private int time;
    private int type;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public int getIndexID() {
        return indexID;
    }

    public void setIndexID(int indexID) {
        this.indexID = indexID;
    }

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
