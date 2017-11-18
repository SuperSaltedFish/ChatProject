package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.yzx.chat.bean.ContactBean;

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
    private static final int COLUMN_INDEX_UserTo = 0;
    private static final int COLUMN_INDEX_UserFrom = 1;
    private static final int COLUMN_INDEX_Type = 2;
    private static final int COLUMN_INDEX_Reason = 3;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserTo + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserFrom + " TEXT NOT NULL , "
                    + COLUMN_NAME_Type + " TEXT NOT NULL,"
                    + COLUMN_NAME_Reason + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserTo + ", " + COLUMN_NAME_UserFrom + ")"
                    + ")";


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
        return values;
    }

    @Override
    protected ContactBean toEntity(Cursor cursor) {
        ContactBean bean = new ContactBean();
        bean.setUserTo(cursor.getString(COLUMN_INDEX_UserTo));
        bean.setUserFrom(cursor.getString(COLUMN_INDEX_UserFrom));
        bean.setType(cursor.getString(COLUMN_INDEX_Type));
        bean.setReason(cursor.getString(COLUMN_INDEX_Reason));
        return bean;
    }

}
