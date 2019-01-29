package com.yzx.chat.core.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupEntity implements Parcelable, BasicInfoProvider {

    private String groupID;
    private String name;
    private String createTime;
    private String owner;
    private String notice;
    private String avatar;
    private ArrayList<GroupMemberEntity> members;
    private HashMap<String, GroupMemberEntity> mMembersMap;
    private String avatarUrlFromMembers;

    public String getNameAndMemberNumber() {

        return String.format(Locale.getDefault(), "%s(%d)", name, members == null ? 0 : members.size());
    }

    public String getAvatarUrlFromMembers() {
        if (TextUtils.isEmpty(avatarUrlFromMembers)) {
            int size = members.size();
            if (size > 9) {
                size = 9;
            }
            StringBuilder builder = null;
            String avatar;
            for (int i = 0; i < size; i++) {
                if (builder == null) {
                    builder = new StringBuilder(50 * size);
                }
                avatar = members.get(i).getUserProfile().getAvatar();
                if (TextUtils.isEmpty(avatar)) {
                    builder.append("-");
                } else {
                    builder.append(avatar);
                }

                if (i != size - 1) {
                    builder.append(",");
                }
            }
            if (builder != null) {
                avatarUrlFromMembers = builder.toString();
            }
        }
        return avatarUrlFromMembers;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupEntity)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (TextUtils.isEmpty(groupID)) {
            return false;
        }
        GroupEntity group = (GroupEntity) obj;
        return groupID.equals(group.getGroupID());
    }

    private void tryInitMembersMap() {
        if (members == null || members.size() == 0) {
            mMembersMap = null;
            return;
        }
        if (mMembersMap == null) {
            mMembersMap = new HashMap<>();
        }
        if (mMembersMap.size() != members.size()) {
            mMembersMap.clear();
            for (GroupMemberEntity member : members) {
                mMembersMap.put(member.getUserProfile().getUserID(), member);
            }
        }
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getName(String userID) {
        tryInitMembersMap();
        if (mMembersMap != null) {
            GroupMemberEntity member = mMembersMap.get(userID);
            if (member != null) {
                return member.getAlias();
            }
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getAvatar(String userID) {
        tryInitMembersMap();
        if (mMembersMap != null) {
            GroupMemberEntity member = mMembersMap.get(userID);
            if (member != null) {
                return member.getUserProfile().getAvatar();
            }
        }
        return null;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public ArrayList<GroupMemberEntity> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<GroupMemberEntity> members) {
        this.members = members;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupID);
        dest.writeString(this.name);
        dest.writeString(this.createTime);
        dest.writeString(this.owner);
        dest.writeString(this.avatar);
        dest.writeString(this.notice);
        dest.writeTypedList(this.members);
    }

    public GroupEntity() {
    }

    protected GroupEntity(Parcel in) {
        this.groupID = in.readString();
        this.name = in.readString();
        this.createTime = in.readString();
        this.owner = in.readString();
        this.avatar = in.readString();
        this.notice = in.readString();
        this.members = in.createTypedArrayList(GroupMemberEntity.CREATOR);
    }

    public static final Parcelable.Creator<GroupEntity> CREATOR = new Parcelable.Creator<GroupEntity>() {
        @Override
        public GroupEntity createFromParcel(Parcel source) {
            return new GroupEntity(source);
        }

        @Override
        public GroupEntity[] newArray(int size) {
            return new GroupEntity[size];
        }
    };

    public static GroupEntity copy(GroupEntity group) {
        if (group == null) {
            return null;
        }
        Parcel parcel = Parcel.obtain();
        group.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        group = GroupEntity.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return group;
    }
}
