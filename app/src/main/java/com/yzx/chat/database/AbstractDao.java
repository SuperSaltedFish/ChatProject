package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public abstract class AbstractDao<T> {

    protected static final String COLUMN_NAME_RowID = "ROWID";

    protected ReadWriteHelper mHelper;

    protected abstract String getTableName();

    protected abstract String getWhereClauseOfKey();

    protected abstract String[] toWhereArgsOfKey(T entity);

    protected abstract void parseToContentValues(T entity, ContentValues values);

    protected abstract T toEntity(Cursor cursor);

    public AbstractDao(ReadWriteHelper helper) {
        mHelper = helper;
    }

    public T loadByKey(String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, getWhereClauseOfKey(), keyValues, null, null, null);
        T entity = null;
        if (cursor.moveToFirst()) {
            entity = toEntity(cursor);
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return entity;
    }

    public boolean insert(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values =new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mHelper.openWritableDatabase().insert(getTableName(), null, values) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean insertAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
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
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean replace(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values =new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mHelper.openWritableDatabase().replace(getTableName(), null, values) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean replaceAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
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
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean update(T entity) {
        if (entity == null) {
            return false;
        }
        ContentValues values =new ContentValues();
        parseToContentValues(entity, values);
        boolean result = mHelper.openWritableDatabase().update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean updateAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
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
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean delete(T entity) {
        if (entity == null) {
            return false;
        }
        boolean result = mHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteByKey(String... keyValue) {
        if (keyValue == null || keyValue.length == 0) {
            return false;
        }
        boolean result = mHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(), keyValue) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteAll(Iterable<T> entityIterable) {
        if (entityIterable == null) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
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
        mHelper.closeWritableDatabase();
        return result;
    }

    public void cleanTable() {
        SQLiteDatabase database = mHelper.openWritableDatabase();
        database.execSQL("delete from " + getTableName());
        mHelper.closeWritableDatabase();
    }

    public boolean isExist(T entity) {
        if (entity == null) {
            return false;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, getWhereClauseOfKey(), toWhereArgsOfKey(entity), null, null, null, null);
        boolean result = (cursor.getCount() > 0);
        cursor.close();
        mHelper.closeReadableDatabase();
        return result;
    }

    public interface ReadWriteHelper {
        SQLiteDatabase openReadableDatabase();

        SQLiteDatabase openWritableDatabase();

        void closeReadableDatabase();

        void closeWritableDatabase();
    }
}
