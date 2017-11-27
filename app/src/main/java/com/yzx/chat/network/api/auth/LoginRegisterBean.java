package com.yzx.chat.network.api.auth;


import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class LoginRegisterBean {
    private String token;
    private String secretKey;
    private UserBean user;

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

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }
}
