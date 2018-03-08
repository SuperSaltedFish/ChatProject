package com.yzx.chat.bean;


import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupBean {

    private String groupID;
    private String name;
    private String createTime;
    private String owner;
    private String avatar;
    private String notice;
    private ArrayList<GroupMemberBean> members;

    @Override
    public boolean equals(Object obj) {
        if (obj == null||!(obj instanceof GroupBean)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if(TextUtils.isEmpty(groupID)){
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
}
