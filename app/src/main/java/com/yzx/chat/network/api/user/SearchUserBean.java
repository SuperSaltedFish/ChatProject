package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
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
