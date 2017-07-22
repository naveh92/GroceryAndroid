package com.android_project.grocery.controller.database.local;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by admin on 16/07/2017.
 */
public abstract class AbstractTable {
    protected static SQLiteDatabase readableDB = DatabaseHelper.getInstance().getReadableDatabase();
    protected static SQLiteDatabase writableDB = DatabaseHelper.getInstance().getWritableDatabase();

    public void truncate() {
        writableDB.execSQL("DELETE FROM " + getTableName());
        writableDB.execSQL("VACUUM");
    }

    protected abstract String getTableName();
}