package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.yzx.chat.bean.GroupMember;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupMemberDao extends AbstractDao<GroupMember> {

    static final String TABLE_NAME = "GroupMember";

    private static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_GroupID = "GroupID";
    private static final String COLUMN_NAME_Alias = "Alias";


    private static final int COLUMN_INDEX_UserID = 0;
    private static final int COLUMN_INDEX_GroupID = 1;
    private static final int COLUMN_INDEX_Alias = 2;


    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserID + " TEXT NOT NULL , "
                    + COLUMN_NAME_GroupID + "TEXT NOT NULL,"
                    + COLUMN_NAME_Alias + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + "," + COLUMN_NAME_GroupID + ")"
                    + ")";


    public GroupMemberDao(ReadWriteHelper readWriteHelper) {
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
    protected String[] toWhereArgsOfKey(GroupMember entity) {
        return new String[0];
    }

    @Override
    protected void parseToContentValues(GroupMember entity, ContentValues values) {

    }

    @Override
    protected GroupMember toEntity(Cursor cursor) {
        return null;
    }
}
