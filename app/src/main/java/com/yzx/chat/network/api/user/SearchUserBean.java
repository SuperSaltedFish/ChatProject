package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class SearchUserBean {

    ArrayList<UserBean> userList;

    public ArrayList<UserBean> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<UserBean> userList) {
        this.userList = userList;
    }
}
