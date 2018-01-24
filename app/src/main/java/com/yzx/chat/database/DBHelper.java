package com.yzx.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.yzx.chat.util.LogUtil;


/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class DBHelper {

    private DatabaseHelper mHelper;
    private volatile SQLiteDatabase mReadableDatabase;
    private volatile SQLiteDatabase mWritableDatabase;
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

        private final Object mReadableLock = new Object();
        private final Object mWritableLock = new Object();

        @Override
        public SQLiteDatabase openReadableDatabase() {
            synchronized (mReadableLock) {
                mReadingCount++;
                if (mReadableDatabase == null) {
                    mReadableDatabase = mHelper.getReadableDatabase();
                }
                return mReadableDatabase;
            }
        }

        @Override
        public SQLiteDatabase openWritableDatabase() {
            synchronized (mWritableLock) {
                mWritingCount++;
                if (mWritableDatabase == null) {
                    mWritableDatabase = mHelper.getWritableDatabase();
                }
                return mWritableDatabase;
            }
        }

        @Override
        public void closeReadableDatabase() {
            synchronized (mReadableLock) {
                if (mReadingCount == 0) {
                    return;
                }
                mReadingCount--;
                if (mReadingCount == 0 && mReadableDatabase != null) {
                 //   mReadableDatabase.close();
                  //  mReadableDatabase = null;
                }
            }
        }

        @Override
        public void closeWritableDatabase() {
            synchronized (mWritableLock) {
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
