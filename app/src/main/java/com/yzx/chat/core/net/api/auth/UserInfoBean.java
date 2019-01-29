package com.yzx.chat.core.net.api.auth;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.UserEntity;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年03月06日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class UserInfoBean {

    private String token;
    private String secretKey;
    private UserEntity userProfile;
    private ArrayList<ContactEntity> contacts;
    private ArrayList<GroupEntity> groups;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public UserEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserEntity userProfile) {
        this.userProfile = userProfile;
    }

    public ArrayList<ContactEntity> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<ContactEntity> contacts) {
        this.contacts = contacts;
    }

    public ArrayList<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<GroupEntity> groups) {
        this.groups = groups;
    }
}
