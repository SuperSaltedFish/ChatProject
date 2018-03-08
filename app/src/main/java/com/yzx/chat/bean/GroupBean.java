package com.yzx.chat.bean;


import java.util.ArrayList;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupBean {

    private String groupID;
    private String name;
    private String createTime;
    private String owner;
    private String avatar;
    private String notice;
    private ArrayList<GroupMember> members;
}
