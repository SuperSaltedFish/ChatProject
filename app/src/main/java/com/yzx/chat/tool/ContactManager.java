package com.yzx.chat.tool;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.database.ContactDao;

import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactManager {

    private Map<String,ContactBean> mContactsMap;

    private ContactDao mContactDao;

    public static void init(List<ContactBean> contactList){

    }

    public ContactManager(Map<String, ContactBean> contactsMap) {
        mContactsMap = contactsMap;
    }
}
