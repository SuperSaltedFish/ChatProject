package com.yzx.chat.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.util.PinYinUtil;


/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactBean implements Parcelable {

    private String contactOf;
    private String abbreviation;

    private String userID;
    private String nickname;
    private String avatar;
    private String type;
    private ContactRemarkBean remark;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ContactBean)) {
            return false;
        }
        ContactBean bean = (ContactBean) obj;
        if (TextUtils.isEmpty(bean.getUserID()) || TextUtils.isEmpty(userID)) {
            return false;
        }
        return userID.equals(bean.getUserID());
    }

    public String getName() {
        String remarkName=null;
        if(remark!=null){
            remarkName = remark.getRemarkName();
        }
        return TextUtils.isEmpty(remarkName) ? nickname : remarkName;
    }

    public String getAbbreviation() {
        if (TextUtils.isEmpty(abbreviation)) {
            abbreviation = PinYinUtil.getPinYinAbbreviation(getName(), false);
        }
        return abbreviation;
    }


    public String getContactOf() {
        return contactOf;
    }

    public void setContactOf(String contactOf) {
        this.contactOf = contactOf;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public ContactRemarkBean getRemark() {
        return remark;
    }

    public void setRemark(ContactRemarkBean remark) {
        this.remark = remark;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.contactOf);
        dest.writeString(this.userID);
        dest.writeString(this.nickname);
        dest.writeString(this.avatar);
        dest.writeString(this.type);
        dest.writeString(this.abbreviation);
        dest.writeParcelable(this.remark, flags);
    }

    public ContactBean() {
    }

    protected ContactBean(Parcel in) {
        this.contactOf = in.readString();
        this.userID = in.readString();
        this.nickname = in.readString();
        this.avatar = in.readString();
        this.type = in.readString();
        this.abbreviation = in.readString();
        this.remark = in.readParcelable(ContactRemarkBean.class.getClassLoader());
    }

    public static final Creator<ContactBean> CREATOR = new Creator<ContactBean>() {
        @Override
        public ContactBean createFromParcel(Parcel source) {
            return new ContactBean(source);
        }

        @Override
        public ContactBean[] newArray(int size) {
            return new ContactBean[size];
        }
    };
}
