package com.yzx.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yzx.chat.bean.GroupMember;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class DatabaseHelper extends SQLiteOpenHelper {


    public DatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(UserDao.CREATE_TABLE_SQL);
        db.execSQL(ContactOperationDao.CREATE_TABLE_SQL);
        db.execSQL(ContactDao.CREATE_TABLE_SQL);
        db.execSQL(GroupDao.CREATE_TABLE_SQL);
        db.execSQL(GroupMemberDao.CREATE_TABLE_SQL);
//        db.execSQL(ConversationDao.CREATE_TABLE_SQL);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}