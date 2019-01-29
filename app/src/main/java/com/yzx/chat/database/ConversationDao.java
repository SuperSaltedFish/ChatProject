//package com.yzx.chat.database;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.text.TextUtils;
//
//import com.yzx.chat.bean.ConversationEntity;
//
//import java.util.List;
//
///**
// * Created by YZX on 2017年12月19日.
// * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
// */
//
//public class ConversationDao extends AbstractDao<ConversationEntity> {
//
//    private static final String TABLE_NAME = "Conversation";
//
//    private static final String COLUMN_NAME_UserID = "UserID";
//    private static final String COLUMN_NAME_ConversationID = "ConversationID";
//    private static final String COLUMN_NAME_ConversationType = "ConversationType";
//    private static final String COLUMN_NAME_LastMsgContent = "LastMsgContent";
//    private static final String COLUMN_NAME_LastMsgTime = "LastMsgTime";
//    private static final String COLUMN_NAME_UnreadMsgCount = "UnreadMsgCount";
//
//    private static final int COLUMN_INDEX_UserID = 0;
//    private static final int COLUMN_INDEX_ConversationID = 1;
//    private static final int COLUMN_INDEX_ConversationType = 2;
//    private static final int COLUMN_INDEX_LastMsgContent = 3;
//    private static final int COLUMN_INDEX_LastMsgTime = 4;
//    private static final int COLUMN_INDEX_UnreadMsgCount = 5;
//
//    public static final String CREATE_TABLE_SQL =
//            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
//                    + COLUMN_NAME_UserID + " TEXT NOT NULL, "
//                    + COLUMN_NAME_ConversationID + "  TEXT NOT NULL,"
//                    + COLUMN_NAME_ConversationType + " INTEGER ,"
//                    + COLUMN_NAME_LastMsgContent + " TEXT ,"
//                    + COLUMN_NAME_LastMsgTime + " INTEGER,"
//                    + COLUMN_NAME_UnreadMsgCount + " INTEGER,"
//                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + ", " + COLUMN_NAME_ConversationID + ")"
//                    + ")";
//
//    private static final String INNER_JOIN_SELECT_SQL = String.format("SELECT %s.%s,%s,%s,%s,%s,%s,%s,%s FROM %s INNER JOIN %s ON %s.%s=%s.%s AND %s.%s=?",
//            TABLE_NAME, COLUMN_NAME_UserID,
//            COLUMN_NAME_ConversationID,
//            COLUMN_NAME_ConversationType,
//            COLUMN_NAME_LastMsgContent,
//            COLUMN_NAME_LastMsgTime,
//            COLUMN_NAME_UnreadMsgCount,
//            ContactRemarkDao.COLUMN_NAME_Nickname,
//            ContactRemarkDao.COLUMN_NAME_RemarkName,
//            TABLE_NAME,
//            ContactRemarkDao.TABLE_NAME,
//            TABLE_NAME, COLUMN_NAME_UserID,
//            ContactRemarkDao.TABLE_NAME, ContactRemarkDao.COLUMN_NAME_ContactOf,
//            TABLE_NAME, COLUMN_NAME_UserID);
//
//    public ConversationEntity loadSingleConversation(String userID, String conversation) {
//        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(conversation)) {
//            return null;
//        }
//        SQLiteDatabase database = mHelper.openReadableDatabase();
//        Cursor cursor = database.rawQuery(INNER_JOIN_SELECT_SQL + " WHERE " + COLUMN_NAME_ConversationID + "=?", new String[]{userID,conversation});
//        ConversationEntity bean=null;
//        if (cursor.moveToFirst()) {
//            bean = toEntity(cursor);
//            bean.setNickname(cursor.getString(6));
//            bean.setRemarkName(cursor.getString(7));
//        }
//        cursor.close();
//        mHelper.closeReadableDatabase();
//        return bean;
//    }
//
//    public void loadAllConversationToList(String userID, List<ConversationEntity> container) {
//        if (TextUtils.isEmpty(userID) || container == null) {
//            return;
//        }
//        SQLiteDatabase database = mHelper.openReadableDatabase();
//        Cursor cursor = database.rawQuery(INNER_JOIN_SELECT_SQL , new String[]{userID});
//        while (cursor.moveToNext()) {
//            ConversationEntity bean = toEntity(cursor);
//            bean.setNickname(cursor.getString(6));
//            bean.setRemarkName(cursor.getString(7));
//            container.add(bean);
//        }
//        cursor.close();
//        mHelper.closeReadableDatabase();
//    }
//
//    public int getUnreadMsgCount(String userID) {
//        if (TextUtils.isEmpty(userID)) {
//            return 0;
//        }
//        SQLiteDatabase database = mHelper.openReadableDatabase();
//        Cursor cursor = database.rawQuery("SELECT SUM(" + COLUMN_NAME_UnreadMsgCount + ") FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_UserID + "=?", new String[]{userID});
//        int count = 0;
//        while (cursor.moveToNext()) {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
//        mHelper.closeReadableDatabase();
//        return count;
//    }
//
//    @Override
//    protected String getTableName() {
//        return TABLE_NAME;
//    }
//
//    @Override
//    protected String getWhereClauseOfKey() {
//        return COLUMN_NAME_UserID + "=? and " + COLUMN_NAME_ConversationID + "=?";
//    }
//
//    @Override
//    protected String[] toWhereArgsOfKey(ConversationEntity entity) {
//        return new String[]{entity.getUserID(), entity.getConversationID()};
//    }
//
//    @Override
//    protected ContentValues toContentValues(ConversationEntity entity, ContentValues values) {
//        values.put(COLUMN_NAME_UserID, entity.getUserID());
//        values.put(COLUMN_NAME_ConversationID, entity.getConversationID());
//        values.put(COLUMN_NAME_ConversationType, entity.getConversationType());
//        values.put(COLUMN_NAME_LastMsgContent, entity.getLastMsgContent());
//        values.put(COLUMN_NAME_LastMsgTime, entity.getLastMsgTime());
//        values.put(COLUMN_NAME_UnreadMsgCount, entity.getUnreadMsgCount());
//        return values;
//    }
//
//    @Override
//    protected ConversationEntity toEntity(Cursor cursor) {
//        ConversationEntity bean = new ConversationEntity(cursor.getInt(COLUMN_INDEX_ConversationType));
//        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
//        bean.setConversationID(cursor.getString(COLUMN_INDEX_ConversationID));
//        bean.setLastMsgContent(cursor.getString(COLUMN_INDEX_LastMsgContent));
//        bean.setLastMsgTime(cursor.getLong(COLUMN_INDEX_LastMsgTime));
//        bean.setUnreadMsgCount(cursor.getInt(COLUMN_INDEX_UnreadMsgCount));
//        return bean;
//    }
//}
