package com.yzx.chat.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.PinYinUtil;

/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactBean implements Parcelable {

    private String contactOf;
    private String userID;
    private String nickname;
    private String remarkName;
    private String avatar;

    private String abbreviation;

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
        return TextUtils.isEmpty(remarkName) ? nickname : remarkName;
    }

    public String getAbbreviation() {
        if (TextUtils.isEmpty(abbreviation)) {
            abbreviation = PinYinUtil.getPinYinAbbreviation(getName(), false);
        }
        return abbreviation;
    }


    public String getContactOf() {
        return IdentityManager.getInstance().getUserID();
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

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
        dest.writeString(this.remarkName);
        dest.writeString(this.avatar);
        dest.writeString(this.abbreviation);
    }

    public ContactBean() {
    }

    protected ContactBean(Parcel in) {
        this.contactOf = in.readString();
        this.userID = in.readString();
        this.nickname = in.readString();
        this.remarkName = in.readString();
        this.avatar = in.readString();
        this.abbreviation = in.readString();
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
