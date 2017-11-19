package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public abstract class AbstractDao<T> {

    protected ReadWriteHelper mHelper;

    protected abstract String getTableName();

    protected abstract String getWhereClauseOfKey();

    protected abstract String[] toWhereArgsOfKey(T entity);

    protected abstract ContentValues toContentValues(T entity, ContentValues values);

    protected abstract T toEntity(Cursor cursor);


    public void setReadWriteHelper(ReadWriteHelper helper) {
        mHelper = helper;
    }

    public T loadByKey(String... keyValues) {
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
        ContentValues values = toContentValues(entity, new ContentValues());
        if (values == null) {
            return false;
        }
        boolean result = mHelper.openWritableDatabase().insert(getTableName(), null, values) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean insertAll(Iterable<T> entityIterable) {
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                values = toContentValues(entity, values);
                if (values == null || database.insert(getTableName(), null, values) <= 0) {
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
        ContentValues values = toContentValues(entity, new ContentValues());
        if (values == null) {
            return false;
        }
        boolean result = mHelper.openWritableDatabase().replace(getTableName(), null, values) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean replaceAll(Iterable<T> entityIterable) {
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                values = toContentValues(entity, values);
                if (values == null || database.replace(getTableName(), null, values) <= 0) {
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
        ContentValues values = toContentValues(entity, new ContentValues());
        if (values == null) {
            return false;
        }

        boolean result = mHelper.openWritableDatabase().update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean updateAll(Iterable<T> entityIterable) {
        boolean result = true;
        SQLiteDatabase database = mHelper.openWritableDatabase();
        database.beginTransactionNonExclusive();
        try {
            ContentValues values = new ContentValues();
            for (T entity : entityIterable) {
                values.clear();
                values = toContentValues(entity, values);
                if (values == null || database.update(getTableName(), values, getWhereClauseOfKey(), toWhereArgsOfKey(entity)) <= 0) {
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
        boolean result = mHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(), toWhereArgsOfKey(entity)) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteByKey(String ...keyValue) {
        boolean result = mHelper.openWritableDatabase().delete(getTableName(), getWhereClauseOfKey(),keyValue) > 0;
        mHelper.closeWritableDatabase();
        return result;
    }

    public boolean deleteAll(Iterable<T> entityIterable) {
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

    public boolean isExist(T entity) {
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, getWhereClauseOfKey(), toWhereArgsOfKey(entity), null, null, null,null);
        boolean result = (cursor.getCount() > 0);
        cursor.close();
        return result;
    }


//    private static ContentValues beanToContentValues(Object entity) {
//        ContentValues values = new ContentValues();
//        Field[] Fields = entity.getClass().getDeclaredFields();
//        Class valueType;
//        String fieldName;
//        try {
//            for (Field field : Fields) {
//                field.setAccessible(true);
//                if (field.isAnnotationPresent(Transient.class)) {
//                    continue;
//                }
//                valueType = field.getType();
//                fieldName = field.getName();
//                if (valueType == String.class)
//                    values.put(fieldName, (String) field.get(entity));
//                else if (valueType == int.class || valueType == Integer.class)
//                    values.put(fieldName, field.getInt(entity));
//                else if (valueType == boolean.class || valueType == Boolean.class)
//                    values.put(fieldName, field.getBoolean(entity));
//                else if (valueType == double.class || valueType == Double.class)
//                    values.put(fieldName, field.getDouble(entity));
//                else if (valueType == long.class || valueType == Long.class)
//                    values.put(fieldName, field.getLong(entity));
//                else if (valueType == float.class || valueType == Float.class)
//                    values.put(fieldName, field.getFloat(entity));
//                else if (valueType == short.class || valueType == Short.class)
//                    values.put(fieldName, field.getShort(entity));
//                else if (valueType == byte.class || valueType == Byte.class)
//                    values.put(fieldName, field.getByte(entity));
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    public interface ReadWriteHelper {
        SQLiteDatabase openReadableDatabase();

        SQLiteDatabase openWritableDatabase();

        void closeReadableDatabase();

        void closeWritableDatabase();
    }
}
