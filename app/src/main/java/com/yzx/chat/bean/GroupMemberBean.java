package com.yzx.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupMemberBean implements Parcelable {
    private String alias;
    private int role;
    private String groupID;
    private UserBean userProfile;

    public String getNicknameInGroup() {
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        } else if(userProfile!=null){
            return userProfile.getNickname();
        }else {
            return "";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupMemberBean)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        GroupMemberBean groupMember = (GroupMemberBean) obj;
        return groupID != null &&
                userProfile != null &&
                groupID.equals(groupMember.getGroupID()) &&
                userProfile.getUserID().equals(groupMember.getUserProfile().getUserID());
    }

    @Override
    public int hashCode() {
        if (!TextUtils.isEmpty(groupID) && userProfile != null && !TextUtils.isEmpty(userProfile.getUserID())) {
            return String.format("%s%s", groupID, userProfile.getUserID()).hashCode();
        }
        return super.hashCode();
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getAlias() {
        if(TextUtils.isEmpty(alias)){
            return userProfile.getNickname();
        }
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public UserBean getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserBean userProfile) {
        this.userProfile = userProfile;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.alias);
        dest.writeInt(this.role);
        dest.writeString(this.groupID);
        dest.writeParcelable(this.userProfile, flags);
    }

    public GroupMemberBean() {
    }

    protected GroupMemberBean(Parcel in) {
        this.alias = in.readString();
        this.role = in.readInt();
        this.groupID = in.readString();
        this.userProfile = in.readParcelable(UserBean.class.getClassLoader());
    }

    public static final Creator<GroupMemberBean> CREATOR = new Creator<GroupMemberBean>() {
        @Override
        public GroupMemberBean createFromParcel(Parcel source) {
            return new GroupMemberBean(source);
        }

        @Override
        public GroupMemberBean[] newArray(int size) {
            return new GroupMemberBean[size];
        }
    };

    public static GroupMemberBean copy(GroupMemberBean member) {
        if (member == null) {
            return null;
        }
        Parcel parcel = Parcel.obtain();
        member.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        member = GroupMemberBean.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return member;
    }
}
