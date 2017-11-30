package com.yzx.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class DBHelper {

    private DatabaseHelper mHelper;
    private SQLiteDatabase mReadableDatabase;
    private SQLiteDatabase mWritableDatabase;
    private int mReadingCount;
    private int mWritingCount;


    public DBHelper(Context context, String name, int version) {
        mHelper = new DatabaseHelper(context, name, version);
    }

    public <T extends AbstractDao> T getDaoInstance(T t) {
        t.setReadWriteHelper(mReadWriteHelper);
        return t;
    }

    private final AbstractDao.ReadWriteHelper mReadWriteHelper = new AbstractDao.ReadWriteHelper() {

        @Override
        public SQLiteDatabase openReadableDatabase() {
            synchronized (this) {
                mReadingCount++;
                if (mReadableDatabase == null) {
                    mReadableDatabase = mHelper.getReadableDatabase();
                }
                return mReadableDatabase;
            }
        }

        @Override
        public SQLiteDatabase openWritableDatabase() {
            synchronized (this) {
                mWritingCount++;
                if (mWritableDatabase == null) {
                    mWritableDatabase = mHelper.getWritableDatabase();
                }
                return mWritableDatabase;
            }
        }

        @Override
        public void closeReadableDatabase() {
            synchronized (this) {
                if (mReadingCount == 0) {
                    return;
                }
                mReadingCount--;
                if (mReadingCount == 0 && mReadableDatabase != null) {
                    mReadableDatabase.close();
                    mReadableDatabase = null;
                }
            }
        }

        @Override
        public void closeWritableDatabase() {
            synchronized (this) {
                if (mWritingCount == 0) {
                    return;
                }
                mWritingCount--;
                if (mWritingCount == 0 && mWritableDatabase != null) {
                    mWritableDatabase.close();
                    mWritableDatabase = null;
                }
            }
        }
    };

}
