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

    private static final String TABLE_NAME = "ContactMessage";

    private static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_Type = "Type";
    private static final String COLUMN_NAME_Reason = "Reason";
    private static final String COLUMN_NAME_IsRemind = "IsRemind";
    private static final String COLUMN_NAME_Time = "Time";

    private static final int COLUMN_INDEX_UserID = 0;
    private static final int COLUMN_INDEX_Type = 1;
    private static final int COLUMN_INDEX_Reason = 2;
    private static final int COLUMN_INDEX_IsRemind = 3;
    private static final int COLUMN_INDEX_Time = 4;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserID + "  TEXT NOT NULL,"
                    + COLUMN_NAME_Type + " TEXT NOT NULL,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + COLUMN_NAME_IsRemind + " INTEGER,"
                    + COLUMN_NAME_Time + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + ")"
                    + ")";


    public synchronized List<ContactOperationBean> loadAllContactOperation() {
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_NAME_IsRemind + " DESC," + COLUMN_NAME_Time + " DESC", null);
        List<ContactOperationBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }


    public synchronized List<ContactOperationBean> loadMoreContactOperation( int startID, int count) {
        if (count <= 0) {
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, new String[]{"*", COLUMN_NAME_RowID},  COLUMN_NAME_RowID + "<?", new String[]{ String.valueOf(startID)}, null, null, COLUMN_NAME_RowID + " DESC", String.valueOf(count));
        List<ContactOperationBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }

    public synchronized int loadRemindCount() {
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, new String[]{"COUNT(ROWID)"}, COLUMN_NAME_IsRemind + "=?", new String[]{ "1"}, null, null, null, null);
        int result;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        } else {
            result = 0;
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return result;
    }

    public synchronized int makeAllRemindAsRemind() {
        SQLiteDatabase database = mHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_IsRemind, 0);
        int result = database.update(getTableName(), values, null, null);
        mHelper.closeWritableDatabase();
        return result;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_UserID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactOperationBean entity) {
        return new String[]{entity.getUserID()};
    }

    @Override
    protected ContentValues toContentValues(ContactOperationBean entity, ContentValues values) {
        values.put(ContactOperationDao.COLUMN_NAME_UserID, entity.getUserID());
        values.put(ContactOperationDao.COLUMN_NAME_Type, entity.getType());
        values.put(ContactOperationDao.COLUMN_NAME_Reason, entity.getReason());
        values.put(ContactOperationDao.COLUMN_NAME_IsRemind, entity.isRemind() ? 1 : 0);
        values.put(ContactOperationDao.COLUMN_NAME_Time, entity.getTime());
        return values;
    }

    @Override
    protected ContactOperationBean toEntity(Cursor cursor) {
        ContactOperationBean bean = new ContactOperationBean();
        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
        bean.setType(cursor.getString(COLUMN_INDEX_Type));
        bean.setReason(cursor.getString(COLUMN_INDEX_Reason));
        bean.setRemind(cursor.getInt(COLUMN_INDEX_IsRemind) == 1);
        bean.setTime(cursor.getInt(COLUMN_INDEX_Time));
        bean.setIndexID(cursor.getInt(cursor.getColumnCount() - 1));
        return bean;
    }

}
