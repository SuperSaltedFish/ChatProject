package com.yzx.chat.network.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactOperationDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.database.UserDao;

import java.util.HashMap;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

class DBManager {

    private static DBManager sManager;

    public static void init(Context context, String name, int version) {
        sManager = new DBManager(new DBHelper(context.getApplicationContext(), name, version));
    }

    public static DBManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("DBManager is not initialized");
        }
        return sManager;
    }

    private DBHelper mDBHelper;
    private HashMap<Class, Object> mDaoInstanceMap;

    private DBManager(DBHelper DBHelper) {
        mDBHelper = DBHelper;
        mDaoInstanceMap = new HashMap<>(16);
    }

    public synchronized void destroy() {
        if (sManager != null) {
            mDBHelper.destroy();
            mDaoInstanceMap.clear();
            mDBHelper = null;
            mDaoInstanceMap = null;
            sManager = null;
        }
    }

    public UserDao getUserDao() {
        UserDao dao = (UserDao) mDaoInstanceMap.get(UserDao.class);
        if (dao == null) {
            synchronized (this){
                dao = (UserDao) mDaoInstanceMap.get(UserDao.class);
                if(dao==null){
                    dao = new UserDao(mProxyReadWriteHelper);
                    mDaoInstanceMap.put(UserDao.class, dao);
                }
            }
        }
        return dao;
    }

    public ContactOperationDao getContactOperationDao() {
        ContactOperationDao dao = (ContactOperationDao) mDaoInstanceMap.get(ContactOperationDao.class);
        if (dao == null) {
            synchronized (this){
                dao = (ContactOperationDao) mDaoInstanceMap.get(ContactOperationDao.class);
                if(dao==null){
                    dao = new ContactOperationDao(mProxyReadWriteHelper);
                    mDaoInstanceMap.put(ContactOperationDao.class, dao);
                }
            }
        }
        return dao;
    }

    public ContactDao getContactDao() {
        ContactDao dao = (ContactDao) mDaoInstanceMap.get(ContactDao.class);
        if (dao == null) {
            synchronized (this){
                dao = (ContactDao) mDaoInstanceMap.get(ContactDao.class);
                if(dao==null){
                    dao = new ContactDao(mProxyReadWriteHelper);
                    mDaoInstanceMap.put(ContactDao.class, dao);
                }
            }
        }
        return dao;
    }

    private final AbstractDao.ReadWriteHelper mProxyReadWriteHelper = new AbstractDao.ReadWriteHelper() {

        @Override
        public SQLiteDatabase openReadableDatabase() {
            return null;
        }

        @Override
        public SQLiteDatabase openWritableDatabase() {
            return null;
        }

        @Override
        public void closeReadableDatabase() {

        }

        @Override
        public void closeWritableDatabase() {

        }
    };

//    public ConversationDao getConversationDao() {
//        ConversationDao dao = (ConversationDao) mDaoInstanceMap.get(ConversationDao.class);
//        if (dao == null) {
//            dao = mDBHelper.getDaoInstance(new ConversationDao());
//            mDaoInstanceMap.put(ConversationDao.class, dao);
//        }
//        return dao;
//    }
}
