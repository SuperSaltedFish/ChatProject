package com.yzx.chat.core.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupMemberEntity implements Parcelable {
    private String alias;
    private int role;
    private String groupID;
    private UserEntity userProfile;

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
        if (!(obj instanceof GroupMemberEntity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        GroupMemberEntity groupMember = (GroupMemberEntity) obj;
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
        dest.writeString(this.alias);
        dest.writeInt(this.role);
        dest.writeString(this.groupID);
        dest.writeParcelable(this.userProfile, flags);
    }

    public GroupMemberEntity() {
    }

    protected GroupMemberEntity(Parcel in) {
        this.alias = in.readString();
        this.role = in.readInt();
        this.groupID = in.readString();
        this.userProfile = in.readParcelable(UserEntity.class.getClassLoader());
    }

    public static final Creator<GroupMemberEntity> CREATOR = new Creator<GroupMemberEntity>() {
        @Override
        public GroupMemberEntity createFromParcel(Parcel source) {
            return new GroupMemberEntity(source);
        }

        @Override
        public GroupMemberEntity[] newArray(int size) {
            return new GroupMemberEntity[size];
        }
    };

    public static GroupMemberEntity copy(GroupMemberEntity member) {
        if (member == null) {
            return null;
        }
        Parcel parcel = Parcel.obtain();
        member.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        member = GroupMemberEntity.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return member;
    }
}
