package com.yzx.chat.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public abstract class AbstractDao<T> {

    protected static final String COLUMN_NAME_RowID = "ROWID";

    protected ReadWriteHelper mReadWriteHelper;

    protected abstract String getTableName();

    protected abstract String getWhereClauseOfKey();

    protected abstract String[] toWhereArgsOfKey(T entity);

    protected abstract void parseToContentValues(T entity, ContentValues values);

    protected abstract T toEntity(Cursor cursor);

    public AbstractDao(ReadWriteHelper readWriteHelper) {
        mReadWriteHelper = readWriteHelper;
    }

    public List<T> loadAll() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, null, null, null, null, null);
        List<T> list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            list.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return list;
    }

    public T loadByKey(String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return null;
        }
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, getWhereClauseOfKey(), keyValues, null, null, null);
        T entity = null;
        if (cursor.moveToFirst()) {
            entity = toEntity(cursor);
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return entity;
    }

    public boolean insert(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mReadWriteHelper.openWritableDatabase().insert(getTableName(), null, values) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean insertAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                parseToContentValues(entity, values);
                if (database.insert(getTableName(), null, values) <= 0) {
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

    public boolean replace(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mReadWriteHelper.openWritableDatabase().replace(getTableName(), null, values) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean replaceAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                parseToContentValues(entity, values);
                if (database.replace(getTableName(), null, values) <= 0) {
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

    public boolean update(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mReadWriteHelper.openWritableDatabase().update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean updateAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                parseToContentValues(entity, values);
                if (database.update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) <= 0) {
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

    public boolean delete(T entity) {
        if (entity == null) {
            return false;
        }
        boolean result = mReadWriteHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteByKey(String... keyValue) {
        if (keyValue == null || keyValue.length == 0) {
            return false;
        }
        boolean result = mReadWriteHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(), keyValue) > 0;
        mReadWriteHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            for (T entity : entityIterable) {
                if (database.delete(getTableName(), getWhereClauseOfKey(), toWhereArgsOfKey(entity)) <= 0) {
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

    public void cleanTable() {
        SQLiteDatabase database = mReadWriteHelper.openWritableDatabase();
        database.execSQL("delete from " + getTableName());
        mReadWriteHelper.closeWritableDatabase();
    }

    public boolean isExist(T entity) {
        if (entity == null) {
            return false;
        }
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, getWhereClauseOfKey(), toWhereArgsOfKey(entity), null, null, null, null);
        boolean result = (cursor.getCount() > 0);
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return result;
    }

    public interface ReadWriteHelper {
        SQLiteDatabase openReadableDatabase();

        SQLiteDatabase openWritableDatabase();

        void closeReadableDatabase();

        void closeWritableDatabase();
    }
}
