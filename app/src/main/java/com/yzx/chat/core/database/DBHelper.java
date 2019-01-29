package com.yzx.chat.core.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class DBHelper {

    private DatabaseHelper mHelper;
    private SQLiteDatabase mReadWriteDatabase;
    private final ReentrantReadWriteLock mReadWriteLock;
    private final Lock mReadLock;
    private final Lock mWriteLock;

    public DBHelper(Context context, String name, int version) {
        mHelper = new DatabaseHelper(context, name, version);
        mReadWriteLock = new ReentrantReadWriteLock(true);
        mReadWriteDatabase = mHelper.getWritableDatabase();
        mReadLock = mReadWriteLock.readLock();
        mWriteLock = mReadWriteLock.writeLock();
    }

    public AbstractDao.ReadWriteHelper getReadWriteHelper() {
        return mReadWriteHelper;
    }

    public void destroy() {
        mWriteLock.lock();
        if (mReadWriteDatabase != null) {
            mReadWriteDatabase.close();
            mReadWriteDatabase = null;
        }
        if (mHelper != null) {
            mHelper.close();
        }
        mWriteLock.unlock();
    }

    private final AbstractDao.ReadWriteHelper mReadWriteHelper = new AbstractDao.ReadWriteHelper() {


        @Override
        public SQLiteDatabase openReadableDatabase() {
            if (mReadWriteDatabase == null) {
                throw new RuntimeException("Database already destroy");
            }
            mReadLock.lock();
            return mReadWriteDatabase;
        }

        @Override
        public SQLiteDatabase openWritableDatabase() {
            if (mReadWriteDatabase == null) {
                throw new RuntimeException("Database already destroy");
            }
            mWriteLock.lock();
            return mReadWriteDatabase;
        }

        @Override
        public void closeReadableDatabase() {
            if (mReadWriteDatabase == null) {
                throw new RuntimeException("Database already destroy");
            }
            mReadLock.unlock();
        }

        @Override
        public void closeWritableDatabase() {
            if (mReadWriteDatabase == null) {
                throw new RuntimeException("Database already destroy");
            }
            mWriteLock.unlock();
        }
    };

}
