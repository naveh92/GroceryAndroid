package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 15/04/2017.
 */


public class GroupMembersTable {
    // TODO: For all statements - compile first!

    private static final String TABLE_NAME = "GROUP_MEMBERS";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final String USER_KEY = "USER_KEY";

    // TODO: Primary key as both values
    private static final String CREATE_TABLE_STATEMENT =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + GROUP_KEY + " TEXT, " + USER_KEY + " TEXT);";
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

    public List<String> getGroupMembers(SQLiteDatabase db ,String groupKey) {

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = { USER_KEY };

        // Filter results WHERE USER_KEY = userKey
        String selection = GROUP_KEY + " = ?";
        String[] selectionArgs = { groupKey };

        // Sort the result Cursor
        String sortOrder = USER_KEY + " DESC";

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
        List<String> groupMembersKeys = new ArrayList<>();

        while (cursor.moveToNext()) {
            String currentMemberKey = cursor.getString(cursor.getColumnIndexOrThrow(USER_KEY));
            groupMembersKeys.add(currentMemberKey);
        }

        cursor.close();

        return groupMembersKeys;
    }

    public void insertGroupMembers(SQLiteDatabase db ,String groupKey, List<String> groupMembers) {
        // Insert every memberKey individually.
        for (String memberKey : groupMembers) {
            insert(db, groupKey, memberKey);
        }
    }

    public void insert(SQLiteDatabase db ,String groupKey, String memberKey) {
        // TODO: Is this ok?
//        SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();


        // TODO: Insert or replace?



        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GROUP_KEY, groupKey);
        values.put(USER_KEY, memberKey);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME, null, values);
    }

    public void delete(SQLiteDatabase db ,String groupKey, String userKey) {
//        // Define 'where' part of query.
//        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
//// Specify arguments in placeholder order.
//        String[] selectionArgs = { "MyTitle" };
//// Issue SQL statement.
//        db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);


        // TODO: SQLInjection
        String DELETE_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_KEY + " = '" + groupKey + "' and " + USER_KEY + " = '" + userKey + "'";

        // TODO: db.delete()?
        db.execSQL(DELETE_STATEMENT);
    }

    public void truncate(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}