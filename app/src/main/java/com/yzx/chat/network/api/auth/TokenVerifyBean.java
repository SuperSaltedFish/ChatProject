package com.yzx.chat.network.api.auth;

import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年01月28日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class TokenVerifyBean {
   private UserBean userProfile;

    public UserBean getUser() {
        return userProfile;
    }

    public void setUser(UserBean user) {
        this.userProfile = user;
    }
}
