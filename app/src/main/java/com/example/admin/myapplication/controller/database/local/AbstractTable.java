package com.example.admin.myapplication.controller.database.local;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by admin on 16/07/2017.
 */
public abstract class AbstractTable {
    protected static SQLiteDatabase readableDB = DatabaseHelper.getInstance().getReadableDatabase();
    protected static SQLiteDatabase writableDB = DatabaseHelper.getInstance().getWritableDatabase();

    public void truncate(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + getTableName());
        db.execSQL("VACUUM");
    }

    protected abstract String getTableName();
}