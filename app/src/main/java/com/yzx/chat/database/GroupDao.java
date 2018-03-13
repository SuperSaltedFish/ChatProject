package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by YZX on 2018年03月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupDao extends AbstractDao<GroupBean> {
    static final String TABLE_NAME = "ContactGroup";

    private static final String COLUMN_NAME_GroupID = "GroupID";
    private static final String COLUMN_NAME_Name = "Name";
    private static final String COLUMN_NAME_CreateTime = "CreateTime";
    private static final String COLUMN_NAME_Owner = "Owner";
    private static final String COLUMN_NAME_Avatar = "Avatar";
    private static final String COLUMN_NAME_Notice = "Notice";

    private static final int COLUMN_INDEX_GroupID = 0;
    private static final int COLUMN_INDEX_Name = 1;
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

    public List<GroupBean> loadAllGroup() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + GroupMemberDao.TABLE_NAME + " INNER JOIN " + TABLE_NAME + " USING (" + COLUMN_NAME_GroupID + ") INNER JOIN " + UserDao.TABLE_NAME + " USING(" + UserDao.COLUMN_NAME_UserID + ")", null);
        HashMap<String, GroupBean> groupMap = new HashMap<>();
        GroupBean group;
        String groupID;
        ArrayList<GroupMemberBean> groupMemberList;
        while (cursor.moveToNext()) {
            groupID = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GroupID));
            group = groupMap.get(groupID);
            if (group == null) {
                group = toEntityFromCursor(cursor);
                groupMap.put(groupID, group);
            }
            groupMemberList = group.getMembers();
            if (groupMemberList == null) {
                groupMemberList = new ArrayList<>(32);
                group.setMembers(groupMemberList);
            }
            groupMemberList.add(GroupMemberDao.toEntityFromCursor(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return new ArrayList<>(groupMap.values());
    }

    @Override
    public boolean insertAll(Iterable<GroupBean> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            List<GroupMemberBean> groupMemberList;
            for (GroupBean group : entityIterable) {
                groupMemberList = group.getMembers();
                if (groupMemberList == null || groupMemberList.size() == 0) {
                    result = false;
                    break;
                }
                values.clear();
                parseToContentValues(group, values);
                if (database.insert(TABLE_NAME, null, values) <= 0) {
                    result = false;
                    break;
                }
                for (GroupMemberBean groupMember : groupMemberList) {
                    groupMember.setGroupID(group.getGroupID());
                }
                if (!GroupMemberDao.insertAllGroupMember(database, groupMemberList, values)) {
                    result = false;
                    break;
                }

            }
            if (result) {
                database.setTransactionSuccessful();
            }
        } finally {
            database.endTransaction();
        }
        mReadWriteHelper.closeWritableDatabase();
        return result;

    }

    public boolean updateGroupName(String groupID, String newName) {
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_Name, newName);
        boolean result = database.update(getTableName(), values, getWhereClauseOfKey(), new String[]{groupID}) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean updateGroupNotice(String groupID, String newNotice) {
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_Notice, newNotice);
        boolean result = database.update(getTableName(), values, getWhereClauseOfKey(), new String[]{groupID}) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public GroupDao(ReadWriteHelper readWriteHelper) {
        super(readWriteHelper);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_GroupID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(GroupBean entity) {
        return new String[]{entity.getGroupID()};
    }

    @Override
    protected void parseToContentValues(GroupBean entity, ContentValues values) {
        values.put(COLUMN_NAME_GroupID, entity.getGroupID());
        values.put(COLUMN_NAME_Name, entity.getName());
        values.put(COLUMN_NAME_CreateTime, entity.getCreateTime());
        values.put(COLUMN_NAME_Owner, entity.getOwner());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        values.put(COLUMN_NAME_Notice, entity.getNotice());
    }

    @Override
    protected GroupBean toEntity(Cursor cursor) {
        GroupBean group = new GroupBean();
        group.setGroupID(cursor.getString(COLUMN_INDEX_GroupID));
        group.setName(cursor.getString(COLUMN_INDEX_Name));
        group.setCreateTime(cursor.getString(COLUMN_INDEX_CreateTime));
        group.setOwner(cursor.getString(COLUMN_INDEX_Owner));
        group.setAvatar(cursor.getString(COLUMN_INDEX_Avatar));
        group.setNotice(cursor.getString(COLUMN_INDEX_Notice));
        return group;
    }

    public static GroupBean toEntityFromCursor(Cursor cursor) {
        GroupBean group = new GroupBean();
        group.setGroupID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GroupID)));
        group.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Name)));
        group.setCreateTime(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CreateTime)));
        group.setOwner(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Owner)));
        group.setAvatar(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Avatar)));
        group.setNotice(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Notice)));
        return group;
    }

}
