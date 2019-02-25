package com.yzx.chat.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationDao extends AbstractDao<ContactOperationEntity> {

    static final String TABLE_NAME = "ContactOperation";

    private static final String COLUMN_NAME_ContactID = "ContactID";
    private static final String COLUMN_NAME_Type = "Type";
    private static final String COLUMN_NAME_Reason = "Reason";
    private static final String COLUMN_NAME_IsRemind = "IsRemind";
    private static final String COLUMN_NAME_Time = "Time";

    private static final int COLUMN_INDEX_ContactID = 0;
    private static final int COLUMN_INDEX_Type = 1;
    private static final int COLUMN_INDEX_Reason = 2;
    private static final int COLUMN_INDEX_IsRemind = 3;
    private static final int COLUMN_INDEX_Time = 4;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_ContactID + "  TEXT NOT NULL CHECK(" + COLUMN_NAME_ContactID + "!=''),"
                    + COLUMN_NAME_Type + " TEXT NOT NULL,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + COLUMN_NAME_IsRemind + " INTEGER,"
                    + COLUMN_NAME_Time + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_ContactID + ")"
                    + ")";


    public ContactOperationDao(ReadWriteHelper helper) {
        super(helper);
    }


    public synchronized List<ContactOperationEntity> loadAllContactOperation() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + COLUMN_NAME_ContactID + "=" + UserDao.COLUMN_NAME_UserID + " ORDER BY " + COLUMN_NAME_IsRemind + " DESC," + COLUMN_NAME_Time + " DESC", null);
        List<ContactOperationEntity> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contactList;
    }

    public synchronized int loadRemindCount() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_IsRemind + "=1", null);
        int result;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        } else {
            result = 0;
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return result;
    }

    public synchronized int makeAllRemindAsRemind() {
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_IsRemind, 0);
        int result = database.update(getTableName(), values, null, null);
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public ContactOperationEntity loadByKeyEx(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + COLUMN_NAME_ContactID + "=" + UserDao.COLUMN_NAME_UserID + " AND " + COLUMN_NAME_ContactID + "=?", new String[]{key});
        ContactOperationEntity entity = null;
        if (cursor.moveToFirst()) {
            entity = toEntity(cursor);
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return entity;
    }

    public boolean insertEx(ContactOperationEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean result = false;
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        database.beginTransactionNonExclusive();
        try {
            UserEntity userInfo = entity.getUser();
            if (database.insert(getTableName(), null, values) > 0 && (userInfo == null || UserDao.replaceUser(database, userInfo, values))) {
                database.setTransactionSuccessful();
                result = true;
            }
        } finally {
            database.endTransaction();
        }
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }


    public boolean replaceEx(ContactOperationEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean result = false;
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        database.beginTransactionNonExclusive();
        try {
            UserEntity userInfo = entity.getUser();
            if (database.replace(getTableName(), null, values) > 0 && (userInfo == null || UserDao.replaceUser(database, userInfo, values))) {
                database.setTransactionSuccessful();
                result = true;
            }
        } finally {
            database.endTransaction();
        }
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean updateEx(ContactOperationEntity entity) {
        if (entity == null) {
            return false;
        }
        UserEntity userInfo = entity.getUser();
        if (userInfo == null) {
            return false;
        }
        boolean result = false;
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        database.beginTransactionNonExclusive();
        try {
            if (database.update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0 && UserDao.replaceUser(database, userInfo, values)) {
                database.setTransactionSuccessful();
                result = true;
            }
        } finally {
            database.endTransaction();
        }
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_ContactID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactOperationEntity entity) {
        return new String[]{entity.getContactOperationID()};
    }

    @Override
    protected void parseToContentValues(ContactOperationEntity entity, ContentValues values) {
        values.put(ContactOperationDao.COLUMN_NAME_ContactID, entity.getContactOperationID());
        values.put(ContactOperationDao.COLUMN_NAME_Type, entity.getType());
        values.put(ContactOperationDao.COLUMN_NAME_Reason, entity.getReason());
        values.put(ContactOperationDao.COLUMN_NAME_IsRemind, entity.isRemind() ? 1 : 0);
        values.put(ContactOperationDao.COLUMN_NAME_Time, entity.getTime());
    }

    @Override
    protected ContactOperationEntity toEntity(Cursor cursor) {
        ContactOperationEntity bean = new ContactOperationEntity();
        bean.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Type)));
        bean.setReason(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Reason)));
        bean.setRemind(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_IsRemind)) == 1);
        bean.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_Time)));
        bean.setContactOperationID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Time)));
        bean.setUser(UserDao.toEntityFromCursor(cursor));
        return bean;
    }

}
