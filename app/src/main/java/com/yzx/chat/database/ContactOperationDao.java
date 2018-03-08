package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.ContactOperationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactOperationDao extends AbstractDao<ContactOperationBean> {

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
                    + COLUMN_NAME_ContactID + "  TEXT NOT NULL,"
                    + COLUMN_NAME_Type + " TEXT NOT NULL,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + COLUMN_NAME_IsRemind + " INTEGER,"
                    + COLUMN_NAME_Time + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_ContactID + ")"
                    + ")";

    public ContactOperationDao(ReadWriteHelper helper) {
        super(helper);
    }


    public synchronized List<ContactOperationBean> loadAllContactOperation() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + COLUMN_NAME_ContactID + "=" + UserDao.COLUMN_NAME_UserID + " ORDER BY " + COLUMN_NAME_IsRemind + " DESC," + COLUMN_NAME_Time + " DESC", null);
        List<ContactOperationBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contactList;
    }


//    public synchronized List<ContactOperationBean> loadMoreContactOperation(int startID, int count) {
//        if (count <= 0) {
//            return null;
//        }
//        SQLiteDatabase database = mHelper.openReadableDatabase();
//        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " LEFT OUTER JOIN " + UserDao.TABLE_NAME + " ON " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=" + UserDao.TABLE_NAME + "." + UserDao.COLUMN_NAME_UserID + " AND " + COLUMN_NAME_RowID + "<?" + " ORDER BY " + COLUMN_NAME_RowID + " DESC limit ?", new String[]{String.valueOf(startID), String.valueOf(count)});
//        List<ContactOperationBean> contactList = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            contactList.add(toEntity(cursor));
//        }
//        cursor.close();
//        mHelper.closeReadableDatabase();
//        return contactList;
//    }

    public synchronized int loadRemindCount() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + COLUMN_NAME_ContactID + "=" + UserDao.COLUMN_NAME_UserID + " AND " + COLUMN_NAME_IsRemind + "=1", null);
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

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_ContactID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactOperationBean entity) {
        return new String[]{entity.getUserID()};
    }

    @Override
    protected void parseToContentValues(ContactOperationBean entity, ContentValues values) {
        values.put(ContactOperationDao.COLUMN_NAME_ContactID, entity.getUserID());
        values.put(ContactOperationDao.COLUMN_NAME_Type, entity.getType());
        values.put(ContactOperationDao.COLUMN_NAME_Reason, entity.getReason());
        values.put(ContactOperationDao.COLUMN_NAME_IsRemind, entity.isRemind() ? 1 : 0);
        values.put(ContactOperationDao.COLUMN_NAME_Time, entity.getTime());
    }

    @Override
    protected ContactOperationBean toEntity(Cursor cursor) {
        ContactOperationBean bean = new ContactOperationBean();
        bean.setUserID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ContactID)));
        bean.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Type)));
        bean.setReason(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Reason)));
        bean.setRemind(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_IsRemind)) == 1);
        bean.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_Time)));
        bean.setIndexID(cursor.getInt(cursor.getColumnCount() - 1));
        bean.setUser(UserDao.toEntityFromCursor(cursor));
        return bean;
    }

}
