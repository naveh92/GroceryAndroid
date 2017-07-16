package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by admin on 16/07/2017.
 */
public class LastUpdatedTable extends AbstractTable {
    private static final String TABLE_NAME = "LAST_UPDATED";
    private static final String TABLE = "TABLE_NAME";
    private static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + TABLE + " TEXT," +
                    "                                       " + LAST_UPDATE_TIME + " LONG, " +
                    " PRIMARY KEY (" + TABLE + "));";
    private static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STATEMENT);
    }

    static public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DROP_TABLE_STATEMENT);
        onCreate(db);
    }

    public void setLastUpdateTime(SQLiteDatabase db, String tableName, Long updateTime) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TABLE, tableName);
        values.put(LAST_UPDATE_TIME, updateTime);

        // Insert the new row, returning the primary key value of the new row
        // This will insert if record is new, update otherwise
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Long getLastUpdateTime(SQLiteDatabase db, String tableName) {
        Long lastUpdateTime = 0L;

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = {LAST_UPDATE_TIME};

        // Filter results WHERE TABLE = tableName
        String selection = TABLE + " = ?";
        String[] selectionArgs = {tableName};

        // Sort the result Cursor
        String sortOrder = TABLE + " DESC";

        Cursor cursor = db.query(
                TABLE_NAME,                               // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // Don't group the rows
                null,                                     // Don't filter by row groups
                sortOrder                                 // The sort order
        );

        // Check the results
        if (cursor.moveToNext()) {
            // Extract the data
            lastUpdateTime = Long.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(LAST_UPDATE_TIME)));
        }
        cursor.close();

        return lastUpdateTime;

    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
}

