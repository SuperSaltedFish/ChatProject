package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.GroupMemberBean;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupMemberDao extends AbstractDao<GroupMemberBean> {

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
                    + COLUMN_NAME_GroupID + " TEXT NOT NULL,"
                    + COLUMN_NAME_Alias + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + "," + COLUMN_NAME_GroupID + ")"
                    + ")";


    public GroupMemberDao(ReadWriteHelper readWriteHelper) {
        super(readWriteHelper);
    }

    public boolean updateMemberAlias(String groupID, String memberID, String newAlias) {
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_Alias, newAlias);
        boolean result = database.update(getTableName(), values, getWhereClauseOfKey(), new String[]{memberID, groupID}) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_UserID + "=? AND " + COLUMN_NAME_GroupID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(GroupMemberBean entity) {
        return new String[]{entity.getUserProfile().getUserID(), entity.getGroupID()};
    }

    @Override
    protected void parseToContentValues(GroupMemberBean entity, ContentValues values) {
        values.put(COLUMN_NAME_UserID, entity.getUserProfile().getUserID());
        values.put(COLUMN_NAME_GroupID, entity.getGroupID());
        values.put(COLUMN_NAME_Alias, entity.getAlias());
    }

    @Override
    protected GroupMemberBean toEntity(Cursor cursor) {
        return null;
    }

    static GroupMemberBean toEntityFromCursor(Cursor cursor) {
        GroupMemberBean groupMember = new GroupMemberBean();
        groupMember.setUserProfile(UserDao.toEntityFromCursor(cursor));
        groupMember.setAlias(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Alias)));
        return groupMember;
    }

    static boolean insertAllGroupMember(SQLiteDatabase Write, Iterable<GroupMemberBean> groupMemberList, ContentValues values) {
        boolean result = true;
        for (GroupMemberBean groupMember : groupMemberList) {
            values.clear();
            values.put(COLUMN_NAME_UserID, groupMember.getUserProfile().getUserID());
            values.put(COLUMN_NAME_GroupID, groupMember.getGroupID());
            values.put(COLUMN_NAME_Alias, groupMember.getAlias());
            if (Write.insert(TABLE_NAME, null, values) <= 0) {
                result = false;
                break;
            }
            if (!UserDao.replaceUser(Write, groupMember.getUserProfile(), values)) {
                result = false;
                break;
            }
        }
        return result;
    }
}
