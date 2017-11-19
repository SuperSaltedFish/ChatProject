package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.ContactBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactDao extends AbstractDao<ContactBean> {

    private static final String TABLE_NAME = "Contact";

    private static final String COLUMN_NAME_UserTo = "UserTo";
    private static final String COLUMN_NAME_UserFrom = "UserFrom";
    private static final String COLUMN_NAME_Type = "Type";
    private static final String COLUMN_NAME_Reason = "Reason";
    private static final String COLUMN_NAME_Remind = "Remind";
    private static final int COLUMN_INDEX_UserTo = 0;
    private static final int COLUMN_INDEX_UserFrom = 1;
    private static final int COLUMN_INDEX_Type = 2;
    private static final int COLUMN_INDEX_Reason = 3;
    private static final int COLUMN_INDEX_Remind = 4;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserTo + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserFrom + " TEXT NOT NULL , "
                    + COLUMN_NAME_Type + " TEXT NOT NULL,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + COLUMN_NAME_Remind + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserTo + ", " + COLUMN_NAME_UserFrom + ")"
                    + ")";

    public List<ContactBean> loadAllRemind(String userID){
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_Remind + "=?", new String[]{"1"});
        List<ContactBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()){
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }

    public int loadRemindCount() {
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_Remind + "=?", new String[]{"1"});
        int result;
        if(cursor.moveToFirst()){
            result = cursor.getInt(0);
        }else {
            result=0;
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return result;
    }

    public int makeAllRemindAsNoRemind(String userID) {
        SQLiteDatabase database = mHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_Remind, 0);
        int result = database.update(getTableName(), values, COLUMN_NAME_UserTo + "=?", new String[]{userID});
        mHelper.closeWritableDatabase();
        return result;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_UserTo + "=? and " + COLUMN_NAME_UserFrom + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactBean entity) {
        return new String[]{entity.getUserTo(), entity.getUserFrom()};
    }

    @Override
    protected ContentValues toContentValues(ContactBean entity, ContentValues values) {
        values.put(ContactDao.COLUMN_NAME_UserTo, entity.getUserTo());
        values.put(ContactDao.COLUMN_NAME_UserFrom, entity.getUserFrom());
        values.put(ContactDao.COLUMN_NAME_Type, entity.getType());
        values.put(ContactDao.COLUMN_NAME_Reason, entity.getReason());
        values.put(ContactDao.COLUMN_NAME_Remind, entity.isRemind() ? 1 : 0);
        return values;
    }

    @Override
    protected ContactBean toEntity(Cursor cursor) {
        ContactBean bean = new ContactBean();
        bean.setUserTo(cursor.getString(COLUMN_INDEX_UserTo));
        bean.setUserFrom(cursor.getString(COLUMN_INDEX_UserFrom));
        bean.setType(cursor.getString(COLUMN_INDEX_Type));
        bean.setReason(cursor.getString(COLUMN_INDEX_Reason));
        bean.setRemind(cursor.getInt(COLUMN_INDEX_Remind) == 1);
        return bean;
    }

}
