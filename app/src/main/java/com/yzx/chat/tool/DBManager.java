package com.yzx.chat.tool;

import android.content.Context;

import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.database.FriendDao;

import java.util.HashMap;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class DBManager {

    private static DBManager sManager;

    public static void init(Context context, String name, int version) {
        sManager = new DBManager(new DBHelper(context.getApplicationContext(), name, version));
    }

    public static DBManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sManager;
    }

    private DBHelper mDBHelper;
    private HashMap<Class, Object> mDaoInstanceMap;

    private DBManager(DBHelper DBHelper) {
        mDBHelper = DBHelper;
        mDaoInstanceMap = new HashMap<>(16);
    }

    public ContactDao getContactDao() {
        ContactDao dao = (ContactDao) mDaoInstanceMap.get(ContactDao.class);
        if (dao == null) {
            dao = mDBHelper.getDaoInstance(new ContactDao());
            mDaoInstanceMap.put(ContactDao.class, dao);
        }
        return dao;
    }

    public FriendDao getFriendDao() {
        FriendDao dao = (FriendDao) mDaoInstanceMap.get(FriendDao.class);
        if (dao == null) {
            dao = mDBHelper.getDaoInstance(new FriendDao());
            mDaoInstanceMap.put(FriendDao.class, dao);
        }
        return dao;
    }
}
