package com.android_project.grocery.controller.database.local;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by admin on 16/07/2017.
 */
public abstract class AbstractTable {
    private static SQLiteDatabase readableDB = DatabaseHelper.getInstance().getReadableDatabase();
    private static SQLiteDatabase writableDB = DatabaseHelper.getInstance().getWritableDatabase();

    protected static SQLiteDatabase getReadableDB() {
        if (readableDB == null || !readableDB.isOpen()) {
            readableDB = DatabaseHelper.getInstance().getReadableDatabase();
        }

        return readableDB;
    }
    protected static SQLiteDatabase getWritableDB() {
        if (writableDB == null || !writableDB.isOpen()) {
            writableDB = DatabaseHelper.getInstance().getWritableDatabase();
        }

        return writableDB;
    }

    public void truncate() {
        SQLiteDatabase writeDB = getWritableDB();

        writeDB.execSQL("DELETE FROM " + getTableName());
        writeDB.execSQL("VACUUM");
    }

    protected abstract String getTableName();
}