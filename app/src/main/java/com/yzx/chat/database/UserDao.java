package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class UserDao extends AbstractDao<UserBean> {

    public static final String TABLE_NAME = "Account";

    public static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_Telephone = "Telephone";
    private static final String COLUMN_NAME_Nickname = "Nickname";
    private static final String COLUMN_NAME_Avatar = "Avatar";
    private static final String COLUMN_NAME_Signature = "Signature";
    private static final String COLUMN_NAME_Location = "Location";
    private static final String COLUMN_NAME_Birthday = "Birthday";
    private static final String COLUMN_NAME_Sex = "Sex";


    private static final int COLUMN_INDEX_UserID = 0;
    private static final int COLUMN_INDEX_Telephone = 1;
    private static final int COLUMN_INDEX_Nickname = 2;
    private static final int COLUMN_INDEX_Avatar = 3;
    private static final int COLUMN_INDEX_Signature = 4;
    private static final int COLUMN_INDEX_Location = 5;
    private static final int COLUMN_INDEX_Birthday = 6;
    private static final int COLUMN_INDEX_Sex = 7;


    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserID + " TEXT NOT NULL, "
                    + COLUMN_NAME_Telephone + "  TEXT,"
                    + COLUMN_NAME_Nickname + " TEXT NOT NULL,"
                    + COLUMN_NAME_Avatar + " TEXT,"
                    + COLUMN_NAME_Signature + " TEXT,"
                    + COLUMN_NAME_Location + " TEXT,"
                    + COLUMN_NAME_Birthday + " TEXT,"
                    + COLUMN_NAME_Sex + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + ")"
                    + ")";

    public UserDao(ReadWriteHelper helper) {
        super(helper);
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
    protected String[] toWhereArgsOfKey(UserBean entity) {
        return new String[]{entity.getUserID()};
    }

    @Override
    protected void parseToContentValues(UserBean entity, ContentValues values) {
        values.put(COLUMN_NAME_Telephone, entity.getTelephone());
        values.put(COLUMN_NAME_UserID, entity.getUserID());
        values.put(COLUMN_NAME_Nickname, entity.getNickname());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        values.put(COLUMN_NAME_Signature, entity.getSex());
        values.put(COLUMN_NAME_Location, entity.getLocation());
        values.put(COLUMN_NAME_Birthday, entity.getBirthday());
        values.put(COLUMN_NAME_Sex, entity.getSex());
    }

    @Override
    protected UserBean toEntity(Cursor cursor) {
        UserBean bean = new UserBean();
        bean.setTelephone(cursor.getString(COLUMN_INDEX_Telephone));
        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
        bean.setNickname(cursor.getString(COLUMN_INDEX_Nickname));
        bean.setAvatar(cursor.getString(COLUMN_INDEX_Avatar));
        bean.setSignature(cursor.getString(COLUMN_INDEX_Signature));
        bean.setLocation(cursor.getString(COLUMN_INDEX_Location));
        bean.setBirthday(cursor.getString(COLUMN_INDEX_Birthday));
        bean.setSex(cursor.getInt(COLUMN_INDEX_Sex));
        return bean;
    }

    static UserBean toEntityFromCursor(Cursor cursor) {
        UserBean bean = new UserBean();
        bean.setTelephone(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Telephone)));
        bean.setUserID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_UserID)));
        bean.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Nickname)));
        bean.setAvatar(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Avatar)));
        bean.setSignature(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Signature)));
        bean.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Location)));
        bean.setBirthday(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Birthday)));
        bean.setSex(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_Sex)));
        return bean;
    }

    static boolean replaceUser(SQLiteDatabase Write, UserBean entity, ContentValues values) {
        if(entity==null){
            return false;
        }
        values.clear();
        values.put(COLUMN_NAME_Telephone, entity.getTelephone());
        values.put(COLUMN_NAME_UserID, entity.getUserID());
        values.put(COLUMN_NAME_Nickname, entity.getNickname());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        values.put(COLUMN_NAME_Signature, entity.getSex());
        values.put(COLUMN_NAME_Location, entity.getLocation());
        values.put(COLUMN_NAME_Birthday, entity.getBirthday());
        values.put(COLUMN_NAME_Sex, entity.getSex());
        return Write.replace(TABLE_NAME, null, values) > 0;
    }
}
