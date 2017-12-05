package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.FriendBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class GetUserFriendsBean {

    ArrayList<FriendBean> friends;

    public ArrayList<FriendBean> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<FriendBean> friends) {
        this.friends = friends;
    }
}
