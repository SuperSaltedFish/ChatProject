package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class UserDao extends AbstractDao<UserBean> {

    private static final String TABLE_NAME = "Account";

    private static final String COLUMN_NAME_Account = "Account";
    private static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_Nickname = "Nickname";
    private static final String COLUMN_NAME_Avatar = "Avatar";

    private static final int COLUMN_INDEX_Account = 0;
    private static final int COLUMN_INDEX_UserID = 1;
    private static final int COLUMN_INDEX_Nickname = 2;
    private static final int COLUMN_INDEX_Avatar = 3;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_Account + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserID + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_NAME_Nickname + " TEXT NOT NULL,"
                    + COLUMN_NAME_Avatar + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + ")"
                    + ")";


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_UserID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(UserBean entity) {
        return new String[]{entity.getUserID()};
    }

    @Override
    protected ContentValues toContentValues(UserBean entity, ContentValues values) {
        values.put(COLUMN_NAME_Account, entity.getAccount());
        values.put(COLUMN_NAME_UserID, entity.getUserID());
        values.put(COLUMN_NAME_Nickname, entity.getNickname());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        return values;
    }

    @Override
    protected UserBean toEntity(Cursor cursor) {
        UserBean bean = new UserBean();
        bean.setAccount(cursor.getString(COLUMN_INDEX_Account));
        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
        bean.setNickname(cursor.getString(COLUMN_INDEX_Nickname));
        bean.setAvatar(cursor.getString(COLUMN_INDEX_Avatar));
        return bean;
    }
}
