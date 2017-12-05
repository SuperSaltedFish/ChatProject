package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class GetUserProfileBean {

    private UserBean userProfile;

    public UserBean getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserBean userProfile) {
        this.userProfile = userProfile;
    }
}
