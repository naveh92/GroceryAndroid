package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 6/24/2017.
 */

public class GroupsTable {
    private static final String TABLE_NAME = "GROUPS";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final String TITLE = "TITLE";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + GROUP_KEY + " TEXT," +
                    "                                       " + TITLE + " TEXT);";
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

    public void addNewGroup(SQLiteDatabase db, Group group) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GROUP_KEY, group.getKey());
        values.put(TITLE, group.getTitle());

        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_NAME, null, values);
    }

    public void deleteGroup(SQLiteDatabase db, String groupKey) {
        // TODO: SQLInjection
        String DELETE_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_KEY + " = '" + groupKey + "'";

        // TODO: db.delete()?
        db.execSQL(DELETE_STATEMENT);
    }

    public Group getGroupByKey(SQLiteDatabase db, String groupKey) {
        Group group = null;

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = {GROUP_KEY, TITLE};

        // Filter results WHERE GROUP_KEY = groupKey
        String selection = GROUP_KEY + " = ?";
        String[] selectionArgs = {groupKey};

        // Sort the result Cursor
        String sortOrder = TITLE + " DESC";

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
            String key = cursor.getString(cursor.getColumnIndexOrThrow(GROUP_KEY));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));

            group = new Group(key, title);
        }
        cursor.close();

        return group;
    }
}