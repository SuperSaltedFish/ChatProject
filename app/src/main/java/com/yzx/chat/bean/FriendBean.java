package com.yzx.chat.bean;

import com.yzx.chat.util.PinYinUtil;

/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class FriendBean {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAbbreviation(){
       return PinYinUtil.getPinYinAbbreviation(name,false);
    }
}
