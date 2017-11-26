package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.FriendBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class GetUserFriendsBean {

    ArrayList<FriendBean> userList;

    public ArrayList<FriendBean> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<FriendBean> userList) {
        this.userList = userList;
    }
}
