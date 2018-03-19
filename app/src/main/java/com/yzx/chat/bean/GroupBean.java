package com.yzx.chat.bean;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.R;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupBean implements Parcelable {

    private String groupID;
    private String name;
    private String createTime;
    private String owner;
    private String notice;
    private String avatar;
    private ArrayList<GroupMemberBean> members;

    private String avatarUrlFromMember;

    public String getAvatarUrlFromMember() {
        if (TextUtils.isEmpty(avatarUrlFromMember)) {
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
                avatarUrlFromMember = builder.toString();
            }
        }
        return avatarUrlFromMember;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GroupBean)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (TextUtils.isEmpty(groupID)) {
            return false;
        }
        GroupBean group = (GroupBean) obj;
        return groupID.equals(group.getGroupID());
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public ArrayList<GroupMemberBean> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<GroupMemberBean> members) {
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

    public GroupBean() {
    }

    protected GroupBean(Parcel in) {
        this.groupID = in.readString();
        this.name = in.readString();
        this.createTime = in.readString();
        this.owner = in.readString();
        this.avatar = in.readString();
        this.notice = in.readString();
        this.members = in.createTypedArrayList(GroupMemberBean.CREATOR);
    }

    public static final Parcelable.Creator<GroupBean> CREATOR = new Parcelable.Creator<GroupBean>() {
        @Override
        public GroupBean createFromParcel(Parcel source) {
            return new GroupBean(source);
        }

        @Override
        public GroupBean[] newArray(int size) {
            return new GroupBean[size];
        }
    };
}
