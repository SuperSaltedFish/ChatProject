package com.yzx.chat.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.util.PinYinUtil;


/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactBean implements Parcelable,BasicInfoProvider {

    private UserBean userProfile;
    private ContactRemarkBean remark;

    private String abbreviation;

    public String getName() {
        String remarkName = null;
        if (remark != null) {
            remarkName = remark.getRemarkName();
        }
        return TextUtils.isEmpty(remarkName) ? userProfile.getNickname() : remarkName;
    }

    @Override
    public String getName(int position) {
        return getName();
    }

    @Override
    public String getAvatar() {
        return userProfile==null?null:userProfile.getAvatar();
    }

    @Override
    public String getAvatar(int position) {
        return getAvatar();
    }

    public String getAbbreviation() {
        if (TextUtils.isEmpty(abbreviation)) {
            abbreviation = PinYinUtil.getPinYinAbbreviation(getName(), false);
        }
        return abbreviation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ContactBean)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ContactBean contact = (ContactBean) obj;
        if (userProfile == null || !userProfile.equals(contact.getUserProfile())) {
            return false;
        }
        return true;
    }

    public UserBean getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserBean userProfile) {
        this.userProfile = userProfile;
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
        dest.writeParcelable(this.userProfile, flags);
        dest.writeParcelable(this.remark, flags);
        dest.writeString(this.abbreviation);
    }

    public ContactBean() {
    }

    protected ContactBean(Parcel in) {
        this.userProfile = in.readParcelable(UserBean.class.getClassLoader());
        this.remark = in.readParcelable(ContactRemarkBean.class.getClassLoader());
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
