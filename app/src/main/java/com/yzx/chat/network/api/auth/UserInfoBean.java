package com.yzx.chat.network.api.auth;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.UserBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年03月06日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class UserInfoBean {

    private String token;
    private String secretKey;
    private UserBean userProfile;
    private ArrayList<ContactBean> contacts;

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

    public UserBean getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserBean userProfile) {
        this.userProfile = userProfile;
    }

    public ArrayList<ContactBean> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<ContactBean> contacts) {
        this.contacts = contacts;
    }
}
