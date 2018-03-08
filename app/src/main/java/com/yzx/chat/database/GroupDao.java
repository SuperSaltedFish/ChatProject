package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupDao extends AbstractDao<GroupBean> {
     static final String TABLE_NAME = "Group";

    private static final String COLUMN_NAME_GroupID = "GroupID";
    private static final String COLUMN_NAME_Name = "Name";
    private static final String COLUMN_NAME_CreateTime = "CreateTime";
    private static final String COLUMN_NAME_Owner = "Owner";
    private static final String COLUMN_NAME_Avatar = "Avatar";
    private static final String COLUMN_NAME_Notice = "Notice";

    private static final int COLUMN_INDEX_GroupID = 0;
    private static final int COLUMN_INDEX_INDEX = 1;
    private static final int COLUMN_INDEX_CreateTime = 2;
    private static final int COLUMN_INDEX_Owner = 3;
    private static final int COLUMN_INDEX_Avatar = 4;
    private static final int COLUMN_INDEX_Notice = 5;


    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_GroupID + " TEXT NOT NULL , "
                    + COLUMN_NAME_Name + " TEXT,"
                    + COLUMN_NAME_CreateTime + " TEXT,"
                    + COLUMN_NAME_Owner + " TEXT,"
                    + COLUMN_NAME_Avatar + " TEXT,"
                    + COLUMN_NAME_Notice + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_GroupID + ")"
                    + ")";

    public List<GroupBean> loadAllGroup(){
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + GroupMemberDao.TABLE_NAME + " INNER JOIN " + TABLE_NAME + " ON USING(" + COLUMN_NAME_GroupID + ") INNER JOIN " + UserDao.TABLE_NAME + " ON USING(" + UserDao.COLUMN_NAME_UserID + ")", null);
        List<GroupBean> groupList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contactList;

    }

    
    public GroupDao(ReadWriteHelper readWriteHelper) {
        super(readWriteHelper);
    }

    @Override
    protected String getTableName() {
        return null;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return null;
    }

    @Override
    protected String[] toWhereArgsOfKey(GroupBean entity) {
        return new String[0];
    }

    @Override
    protected void parseToContentValues(GroupBean entity, ContentValues values) {

    }

    @Override
    protected GroupBean toEntity(Cursor cursor) {
        return null;
    }
}
