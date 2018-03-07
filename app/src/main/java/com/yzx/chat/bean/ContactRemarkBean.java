package com.yzx.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月23日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactRemarkBean implements Parcelable {

    private String description;
    private String remarkName;
    private ArrayList<String> telephone;
    private ArrayList<String> tags;

    @Expose
    private int uploadFlag ;


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

    public ArrayList<String> getTelephone() {
        return telephone;
    }

    public void setTelephone(ArrayList<String> telephone) {
        this.telephone = telephone;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeString(this.remarkName);
        dest.writeStringList(this.telephone);
        dest.writeStringList(this.tags);
        dest.writeInt(this.uploadFlag);
    }

    public ContactRemarkBean() {
    }

    protected ContactRemarkBean(Parcel in) {
        this.description = in.readString();
        this.remarkName = in.readString();
        this.telephone = in.createStringArrayList();
        this.tags = in.createStringArrayList();
        this.uploadFlag = in.readInt();
    }

    public static final Parcelable.Creator<ContactRemarkBean> CREATOR = new Parcelable.Creator<ContactRemarkBean>() {
        @Override
        public ContactRemarkBean createFromParcel(Parcel source) {
            return new ContactRemarkBean(source);
        }

        @Override
        public ContactRemarkBean[] newArray(int size) {
            return new ContactRemarkBean[size];
        }
    };
}
