package com.yzx.chat.core.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        try {
            db.execSQL(UserDao.CREATE_TABLE_SQL);
            db.execSQL(ContactOperationDao.CREATE_TABLE_SQL);
            db.execSQL(ContactDao.CREATE_TABLE_SQL);
            db.execSQL(GroupDao.CREATE_TABLE_SQL);
            db.execSQL(GroupMemberDao.CREATE_TABLE_SQL);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}