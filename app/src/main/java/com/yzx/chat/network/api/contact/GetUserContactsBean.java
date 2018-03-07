package com.yzx.chat.network.api.contact;

import com.yzx.chat.bean.ContactBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */



public class GetUserContactsBean {

    ArrayList<ContactBean> contacts;

    public ArrayList<ContactBean> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<ContactBean> contacts) {
        this.contacts = contacts;
    }
}
