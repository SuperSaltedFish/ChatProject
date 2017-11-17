package com.yzx.chat.database;

import android.content.Context;
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}