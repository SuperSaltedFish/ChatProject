package com.yzx.chat.core.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.util.PinYinUtil;


/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactEntity implements Parcelable,BasicInfoProvider {

    private UserEntity userProfile;
    private ContactRemarkEntity remark;

    private String abbreviation;

    public String getName() {
        String remarkName = null;
        if (remark != null) {
            remarkName = remark.getRemarkName();
        }
        return TextUtils.isEmpty(remarkName) ? userProfile.getNickname() : remarkName;
    }

    @Override
    public String getName(String userID) {
        return getName();
    }

    @Override
    public String getAvatar() {
        return userProfile==null?null:userProfile.getAvatar();
    }

    @Override
    public String getAvatar(String userID) {
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
        if (obj == null || !(obj instanceof ContactEntity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ContactEntity contact = (ContactEntity) obj;
        if (userProfile == null || !userProfile.equals(contact.getUserProfile())) {
            return false;
        }
        return true;
    }

    public UserEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserEntity userProfile) {
        this.userProfile = userProfile;
    }

    public ContactRemarkEntity getRemark() {
        return remark;
    }

    public void setRemark(ContactRemarkEntity remark) {
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

    public ContactEntity() {
    }

    protected ContactEntity(Parcel in) {
        this.userProfile = in.readParcelable(UserEntity.class.getClassLoader());
        this.remark = in.readParcelable(ContactRemarkEntity.class.getClassLoader());
        this.abbreviation = in.readString();
    }

    public static final Creator<ContactEntity> CREATOR = new Creator<ContactEntity>() {
        @Override
        public ContactEntity createFromParcel(Parcel source) {
            return new ContactEntity(source);
        }

        @Override
        public ContactEntity[] newArray(int size) {
            return new ContactEntity[size];
        }
    };
}
