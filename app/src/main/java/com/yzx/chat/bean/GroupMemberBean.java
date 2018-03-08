package com.yzx.chat.bean;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupMemberBean {
    private String alias;
    private int role;
    private String groupID;
    private UserBean userProfile;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getAlias() {
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
}
