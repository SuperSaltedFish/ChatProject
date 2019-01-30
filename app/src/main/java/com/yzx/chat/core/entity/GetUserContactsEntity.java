package com.yzx.chat.core.entity;

import com.yzx.chat.core.entity.ContactEntity;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */



public class GetUserContactsEntity {

    ArrayList<ContactEntity> contacts;

    public ArrayList<ContactEntity> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<ContactEntity> contacts) {
        this.contacts = contacts;
    }
}
