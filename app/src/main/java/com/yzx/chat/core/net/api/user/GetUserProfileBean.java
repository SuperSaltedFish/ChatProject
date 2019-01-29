package com.yzx.chat.core.net.api.user;

import com.yzx.chat.core.entity.UserEntity;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class GetUserProfileBean {

    private UserEntity userProfile;

    public UserEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserEntity userProfile) {
        this.userProfile = userProfile;
    }
}
