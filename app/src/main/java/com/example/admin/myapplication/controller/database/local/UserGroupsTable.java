package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 15/04/2017.
 */


public class UserGroupsTable {
    // TODO: For all statements - compile first!

    private static final String TABLE_NAME = "USER_GROUPS";
    private static final String USER_KEY = "USER_KEY";
    private static final String GROUP_KEY = "GROUP_KEY";

    // TODO: Primary key as both values
    private static final String CREATE_TABLE_STATEMENT =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + USER_KEY + " TEXT, " + GROUP_KEY + " TEXT);";
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

    public List<String> getUserGroupKeys(SQLiteDatabase db ,String userKey) {


        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = { GROUP_KEY };

        // Filter results WHERE USER_KEY = userKey
        String selection = USER_KEY + " = ?";
        String[] selectionArgs = { userKey };

        // Sort the result Cursor
        String sortOrder = GROUP_KEY + " DESC";

        Cursor cursor = db.query(
                TABLE_NAME,                               // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // Don't group the rows
                null,                                     // Don't filter by row groups
                sortOrder                                 // The sort order
        );

        // Add all the results to a list
        List<String> groupKeys = new ArrayList<>();
        while (cursor.moveToNext()) {
            String currentGroupKey = cursor.getString(cursor.getColumnIndexOrThrow(GROUP_KEY));
            groupKeys.add(currentGroupKey);
        }
        cursor.close();

        return groupKeys;
    }

    public void insertGroupKeys(SQLiteDatabase db ,String userKey, List<String> groupKeys) {
        // Insert every groupKey individually.
        for (String groupKey : groupKeys) {
            insert(db ,userKey, groupKey);
        }
    }

    public void insert(SQLiteDatabase db ,String userKey, String groupKey) {
        // TODO: Insert or replace?



        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(USER_KEY, userKey);
        values.put(GROUP_KEY, groupKey);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME, null, values);
    }

    public void delete(SQLiteDatabase db ,String userKey, String groupKey) {
//        // Define 'where' part of query.
//        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
//// Specify arguments in placeholder order.
//        String[] selectionArgs = { "MyTitle" };
//// Issue SQL statement.
//        db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);


        // TODO: SQLInjection
        String DELETE_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE " + USER_KEY + " = " + userKey + " and " + GROUP_KEY + " = " + groupKey;

        // TODO: db.delete()?
        db.execSQL(DELETE_STATEMENT);
    }

    public void truncate(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}