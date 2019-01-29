package com.yzx.chat.core.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ContactOperationEntity implements Parcelable {


    private int indexID;
    private String type;
    private String reason;
    private UserEntity mUser;
    private boolean isRemind;
    private int time;

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ContactOperationEntity)){
            return false;
        }
        if(this == obj){
            return true;
        }
        ContactOperationEntity operation = (ContactOperationEntity) obj;
        return mUser!=null&&mUser.equals(operation.getUser());
    }

    public int getIndexID() {
        return indexID;
    }

    public void setIndexID(int indexID) {
        this.indexID = indexID;
    }

    public String getUserID() {
        return mUser==null?null:mUser.getUserID();
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

    public UserEntity getUser() {
        return mUser;
    }

    public void setUser(UserEntity user) {
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
        dest.writeString(this.type);
        dest.writeString(this.reason);
        dest.writeParcelable(this.mUser, flags);
        dest.writeByte(this.isRemind ? (byte) 1 : (byte) 0);
        dest.writeInt(this.time);
    }

    public ContactOperationEntity() {
    }

    protected ContactOperationEntity(Parcel in) {
        this.indexID = in.readInt();
        this.type = in.readString();
        this.reason = in.readString();
        this.mUser = in.readParcelable(UserEntity.class.getClassLoader());
        this.isRemind = in.readByte() != 0;
        this.time = in.readInt();
    }

    public static final Creator<ContactOperationEntity> CREATOR = new Creator<ContactOperationEntity>() {
        @Override
        public ContactOperationEntity createFromParcel(Parcel source) {
            return new ContactOperationEntity(source);
        }

        @Override
        public ContactOperationEntity[] newArray(int size) {
            return new ContactOperationEntity[size];
        }
    };
}
