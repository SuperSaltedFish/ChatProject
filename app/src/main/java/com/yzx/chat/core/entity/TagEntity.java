package com.yzx.chat.core.entity;

/**
 * Created by YZX on 2018年06月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class TagEntity {

    private String name;
    private int memberCount;

    public TagEntity() {
    }

    public TagEntity(String name, int memberCount) {
        this.name = name;
        this.memberCount = memberCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
