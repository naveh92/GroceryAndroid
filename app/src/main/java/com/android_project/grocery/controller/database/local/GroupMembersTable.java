package com.android_project.grocery.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 15/04/2017.
 */
public class GroupMembersTable extends AbstractTable {
    private static final String TABLE_NAME = "GROUP_MEMBERS";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final String USER_KEY = "USER_KEY";
    private static final String CREATE_TABLE_STATEMENT =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + GROUP_KEY + " TEXT, " +
                                                                    USER_KEY + " TEXT, " +
                                                                    "PRIMARY KEY (" + GROUP_KEY + ", " +
                                                                                      USER_KEY + "));";
    private static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * These functions may happen before writableDB is initialized.
     */
    static public void onCreate(SQLiteDatabase db) { db.execSQL(CREATE_TABLE_STATEMENT); }
    static public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DROP_TABLE_STATEMENT);
        onCreate(db);
    }

    public List<String> getGroupMembers(String groupKey) {
        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = { USER_KEY };

        // Filter results WHERE USER_KEY = userKey
        String selection = GROUP_KEY + " = ?";
        String[] selectionArgs = { groupKey };

        // Sort the result Cursor
        String sortOrder = USER_KEY + " DESC";

        Cursor cursor = readableDB.query(
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

    public void insertGroupMembers(String groupKey, List<String> groupMembers) {
        // Insert every memberKey individually.
        for (String memberKey : groupMembers) {
            insert(groupKey, memberKey);
        }
    }

    public void insert(String groupKey, String memberKey) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GROUP_KEY, groupKey);
        values.put(USER_KEY, memberKey);

        // Insert the new row, returning the primary key value of the new row
        // This will insert if record is new, update otherwise
        writableDB.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void delete(String groupKey, String userKey) {
        // Define 'where' part of query.
        String selection = GROUP_KEY + " = ? and " + USER_KEY + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { groupKey, userKey };

        // Issue SQL statement.
        writableDB.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public void deleteAllGroupMembers(String groupKey) {
        // Define 'where' part of query.
        String selection = GROUP_KEY + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { groupKey };

        // Issue SQL statement.
        writableDB.delete(TABLE_NAME, selection, selectionArgs);
    }
}