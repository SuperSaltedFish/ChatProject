package com.yzx.chat.core.entity;

/**
 * Created by YZX on 2018年07月21日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface BasicInfoProvider {

    String getName(String userID);

    String getAvatar(String userID);
}
