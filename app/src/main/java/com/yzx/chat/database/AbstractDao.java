package com.yzx.chat.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public abstract class AbstractDao {

    protected ReadWriteHelper mHelper;

    public void setReadWriteHelper(ReadWriteHelper helper) {
        mHelper = helper;
    }


    public interface ReadWriteHelper {
        SQLiteDatabase openReadableDatabase();

        SQLiteDatabase openWritableDatabase();

        void closeReadableDatabase();

        void closeWritableDatabase();
    }
}
