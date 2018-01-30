package com.yzx.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ContactOperationBean implements Parcelable {


    private int indexID;
    private String userTo;
    private String userFrom;
    private String type;
    private String reason;
    private UserBean mUser;
    private boolean isRemind;
    private int time;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UserBean getUser() {
        return mUser;
    }

    public void setUser(UserBean user) {
        mUser = user;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.indexID);
        dest.writeString(this.userTo);
        dest.writeString(this.userFrom);
        dest.writeString(this.type);
        dest.writeString(this.reason);
        dest.writeParcelable(this.mUser, flags);
        dest.writeByte(this.isRemind ? (byte) 1 : (byte) 0);
        dest.writeInt(this.time);
    }

    public ContactOperationBean() {
    }

    protected ContactOperationBean(Parcel in) {
        this.indexID = in.readInt();
        this.userTo = in.readString();
        this.userFrom = in.readString();
        this.type = in.readString();
        this.reason = in.readString();
        this.mUser = in.readParcelable(UserBean.class.getClassLoader());
        this.isRemind = in.readByte() != 0;
        this.time = in.readInt();
    }

    public static final Creator<ContactOperationBean> CREATOR = new Creator<ContactOperationBean>() {
        @Override
        public ContactOperationBean createFromParcel(Parcel source) {
            return new ContactOperationBean(source);
        }

        @Override
        public ContactOperationBean[] newArray(int size) {
            return new ContactOperationBean[size];
        }
    };
}
