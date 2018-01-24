package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactMessageDao extends AbstractDao<ContactMessageBean> {

    private static final String TABLE_NAME = "ContactMessage";

    private static final String COLUMN_NAME_UserTo = "UserTo";
    private static final String COLUMN_NAME_UserFrom = "UserFrom";
    private static final String COLUMN_NAME_Type = "Type";
    private static final String COLUMN_NAME_Reason = "Reason";
    private static final String COLUMN_NAME_IsRemind = "IsRemind";
    private static final String COLUMN_NAME_Time = "Time";
    private static final String COLUMN_NAME_Nickname = "Nickname";
    private static final String COLUMN_NAME_AvatarUrl = "AvatarUrl";

    private static final int COLUMN_INDEX_UserTo = 0;
    private static final int COLUMN_INDEX_UserFrom = 1;
    private static final int COLUMN_INDEX_Type = 2;
    private static final int COLUMN_INDEX_Reason = 3;
    private static final int COLUMN_INDEX_IsRemind = 4;
    private static final int COLUMN_INDEX_Time = 5;
    private static final int COLUMN_INDEX_Nickname = 6;
    private static final int COLUMN_INDEX_AvatarUrl = 7;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserTo + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserFrom + " TEXT NOT NULL , "
                    + COLUMN_NAME_Type + " INTEGER,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + COLUMN_NAME_IsRemind + " INTEGER,"
                    + COLUMN_NAME_Time + " INTEGER,"
                    + COLUMN_NAME_Nickname + " TEXT,"
                    + COLUMN_NAME_AvatarUrl + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserTo + ", " + COLUMN_NAME_UserFrom + ")"
                    + ")";


    public List<ContactMessageBean> loadAllContactMessage(String userID) {
        if (TextUtils.isEmpty(userID)) {
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_UserTo + "=? ORDER BY " + COLUMN_NAME_IsRemind + " DESC," + COLUMN_NAME_Time + " DESC", new String[]{userID});
        List<ContactMessageBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }


    public List<ContactMessageBean> loadMoreContactMessage(String userID, int startID, int count) {
        if (count == 0 || TextUtils.isEmpty(userID)) {
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        LogUtil.e("loadMoreContactMessage 1");
        Cursor cursor = database.query(TABLE_NAME, new String[]{"*", COLUMN_NAME_RowID}, COLUMN_NAME_UserTo + "=? AND " + COLUMN_NAME_RowID + "<?", new String[]{userID, String.valueOf(startID)}, null, null, COLUMN_NAME_RowID + " DESC", String.valueOf(count));
        LogUtil.e("loadMoreContactMessage 2");
        List<ContactMessageBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }

    public int loadRemindCount(String userID) {
        if (TextUtils.isEmpty(userID)) {
            return 0;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        LogUtil.e("loadRemindCount 1");
        Cursor cursor = database.query(TABLE_NAME, new String[]{"COUNT(ROWID)"}, COLUMN_NAME_UserTo + "=?", new String[]{userID}, null, null, null, null);
        LogUtil.e("loadRemindCount 2");
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

    public int makeAllRemindAsRemind(String userID) {
        SQLiteDatabase database = mHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_IsRemind, 0);
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
    protected String[] toWhereArgsOfKey(ContactMessageBean entity) {
        return new String[]{entity.getUserTo(), entity.getUserFrom()};
    }

    @Override
    protected ContentValues toContentValues(ContactMessageBean entity, ContentValues values) {
        values.put(ContactMessageDao.COLUMN_NAME_UserTo, entity.getUserTo());
        values.put(ContactMessageDao.COLUMN_NAME_UserFrom, entity.getUserFrom());
        values.put(ContactMessageDao.COLUMN_NAME_Type, entity.getType());
        values.put(ContactMessageDao.COLUMN_NAME_Reason, entity.getReason());
        values.put(ContactMessageDao.COLUMN_NAME_IsRemind, entity.isRemind() ? 1 : 0);
        values.put(ContactMessageDao.COLUMN_NAME_Time, entity.getTime());
        values.put(ContactMessageDao.COLUMN_NAME_Nickname, entity.getNickname());
        values.put(ContactMessageDao.COLUMN_NAME_AvatarUrl, entity.getAvatarUrl());
        return values;
    }

    @Override
    protected ContactMessageBean toEntity(Cursor cursor) {
        ContactMessageBean bean = new ContactMessageBean();
        bean.setUserTo(cursor.getString(COLUMN_INDEX_UserTo));
        bean.setUserFrom(cursor.getString(COLUMN_INDEX_UserFrom));
        bean.setType(cursor.getInt(COLUMN_INDEX_Type));
        bean.setReason(cursor.getString(COLUMN_INDEX_Reason));
        bean.setRemind(cursor.getInt(COLUMN_INDEX_IsRemind) == 1);
        bean.setTime(cursor.getInt(COLUMN_INDEX_Time));
        bean.setIndexID(cursor.getInt(cursor.getColumnCount() - 1));
        bean.setNickname(cursor.getString(COLUMN_INDEX_Nickname));
        bean.setAvatarUrl(cursor.getString(COLUMN_INDEX_AvatarUrl));
        return bean;
    }

}
