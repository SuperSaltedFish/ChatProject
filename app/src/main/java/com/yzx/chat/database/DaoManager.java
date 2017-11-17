package com.yzx.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class DaoManager {

    private static DaoManager sManager;

    public static void init(Context context, String name, int version) {
        sManager = new DaoManager(context.getApplicationContext(), name, version);
    }

    public static DaoManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sManager;
    }


    private DatabaseHelper mHelper;
    private SQLiteDatabase mReadableDatabase;
    private SQLiteDatabase mWritableDatabase;
    private int mReadingCount;
    private int mWritingCount;


    private DaoManager(Context context, String name, int version) {
        mHelper = new DatabaseHelper(context, name, version);
    }

    public <T extends AbstractDao> T getDaoInstance(Class<T> c) {
        try {
            T instance = c.newInstance();
            instance.setReadWriteHelper(mReadWriteHelper);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(" No default constructor found in " + c.getSimpleName());
        }
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
                if (mReadingCount == 0 || mReadableDatabase != null) {
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
                if (mWritingCount == 0 || mWritableDatabase != null) {
                    mWritableDatabase.close();
                    mWritableDatabase = null;
                }
            }
        }
    };

}
