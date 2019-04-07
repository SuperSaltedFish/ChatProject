package com.yzx.chat.core.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.yzx.chat.util.PinYinUtil;

import java.util.ArrayList;


/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactEntity implements BasicInfoProvider, Parcelable {

    private String contactID;
    private String description;
    private String remarkName;
    private ArrayList<String> telephones;
    private ArrayList<String> tags;
    private UserEntity userProfile;

    @Expose
    private int uploadFlag;

    private String abbreviation;

    public String getName() {
        if (!TextUtils.isEmpty(remarkName)) {
            return remarkName;
        } else if (userProfile != null) {
            return userProfile.getNickname();
        }
        return "";
    }

    @Override
    public String getName(String userID) {
        return getName();
    }

    @Override
    public String getAvatar() {
        return userProfile == null ? null : userProfile.getAvatar();
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
        if (!(obj instanceof ContactEntity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ContactEntity contact = (ContactEntity) obj;
        return TextUtils.equals(contactID, contact.contactID);
    }

    public String getContactID() {
        if (TextUtils.isEmpty(contactID)) {
            contactID = userProfile.getUserID();
        }
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public ArrayList<String> getTelephones() {
        return telephones;
    }

    public void setTelephones(ArrayList<String> telephones) {
        this.telephones = telephones;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public int getUploadFlag() {
        return uploadFlag;
    }

    public void setUploadFlag(int uploadFlag) {
        this.uploadFlag = uploadFlag;
    }

    public UserEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserEntity userProfile) {
        this.userProfile = userProfile;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.contactID);
        dest.writeString(this.description);
        dest.writeString(this.remarkName);
        dest.writeStringList(this.telephones);
        dest.writeStringList(this.tags);
        dest.writeParcelable(this.userProfile, flags);
        dest.writeInt(this.uploadFlag);
        dest.writeString(this.abbreviation);
    }

    public ContactEntity() {
    }

    protected ContactEntity(Parcel in) {
        this.contactID = in.readString();
        this.description = in.readString();
        this.remarkName = in.readString();
        this.telephones = in.createStringArrayList();
        this.tags = in.createStringArrayList();
        this.userProfile = in.readParcelable(UserEntity.class.getClassLoader());
        this.uploadFlag = in.readInt();
        this.abbreviation = in.readString();
    }

    public static final Parcelable.Creator<ContactEntity> CREATOR = new Parcelable.Creator<ContactEntity>() {
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
