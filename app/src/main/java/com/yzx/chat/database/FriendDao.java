package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.bean.FriendBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class FriendDao extends AbstractDao<FriendBean> {

    private static final String TABLE_NAME = "Friend";

    private static final String COLUMN_NAME_FriendOf = "FriendOf";
    private static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_Nickname = "Nickname";
    private static final String COLUMN_NAME_Avatar = "Avatar";
    private static final String COLUMN_NAME_RemarkName = "RemarkName";
    private static final int COLUMN_INDEX_FriendOf = 0;
    private static final int COLUMN_INDEX_UserID = 1;
    private static final int COLUMN_INDEX_Nickname = 2;
    private static final int COLUMN_INDEX_Avatar = 3;
    private static final int COLUMN_INDEX_RemarkName = 4;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_FriendOf + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserID + " TEXT NOT NULL , "
                    + COLUMN_NAME_Nickname + " TEXT NOT NULL,"
                    + COLUMN_NAME_Avatar + " TEXT,"
                    + COLUMN_NAME_RemarkName + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_FriendOf + ", " + COLUMN_NAME_UserID + ")"
                    + ")";


    public List<FriendBean> loadAllFriend(String friendOf){
        if(TextUtils.isEmpty(friendOf)){
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_FriendOf + "=?", new String[]{friendOf});
        List<FriendBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()){
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }

    public void loadAllFriendTo(List<FriendBean> dataList,String friendOf){
        if(TextUtils.isEmpty(friendOf)){
            return ;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_FriendOf + "=?", new String[]{friendOf});
        while (cursor.moveToNext()){
            dataList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
    }


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_FriendOf + "=? and " + COLUMN_NAME_UserID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(FriendBean entity) {
        return new String[]{entity.getFriendOf(), entity.getUserID()};
    }

    @Override
    protected ContentValues toContentValues(FriendBean entity, ContentValues values) {
        values.put(COLUMN_NAME_FriendOf, entity.getFriendOf());
        values.put(COLUMN_NAME_UserID, entity.getUserID());
        values.put(COLUMN_NAME_Nickname, entity.getNickname());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        values.put(COLUMN_NAME_RemarkName, entity.getRemarkName());
        return values;
    }

    @Override
    protected FriendBean toEntity(Cursor cursor) {
        FriendBean bean = new FriendBean();
        bean.setFriendOf(cursor.getString(COLUMN_INDEX_FriendOf));
        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
        bean.setNickname(cursor.getString(COLUMN_INDEX_Nickname));
        bean.setAvatar(cursor.getString(COLUMN_INDEX_Avatar));
        bean.setRemarkName(cursor.getString(COLUMN_INDEX_RemarkName));
        return bean;
    }
}
