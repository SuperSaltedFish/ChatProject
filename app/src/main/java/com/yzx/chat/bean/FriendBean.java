package com.yzx.chat.bean;


import android.text.TextUtils;

import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.PinYinUtil;

/**
 * Created by YZX on 2017年07月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class FriendBean {

    private String friendOf;
    private String userID;
    private String nickname;
    private String remarkName;
    private String avatar;

    private String abbreviation;

    public String getNicknameOrRemarkName(){
       return TextUtils.isEmpty(remarkName)?nickname:remarkName;
    }

    public String getAbbreviation(){
        if(TextUtils.isEmpty(abbreviation)){
             abbreviation = PinYinUtil.getPinYinAbbreviation(getNicknameOrRemarkName(),false);
        }
        return abbreviation;
    }






    public String getFriendOf() {
        return IdentityManager.getInstance().getUserID();
    }

    public void setFriendOf(String friendOf) {
        this.friendOf = friendOf;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
