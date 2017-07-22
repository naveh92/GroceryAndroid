package com.android_project.grocery.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android_project.grocery.model.entities.Group;

/**
 * Created by admin on 6/24/2017.
 */
public class GroupsTable extends AbstractTable {
    private static final String TABLE_NAME = "GROUPS";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final String TITLE = "TITLE";
    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + GROUP_KEY + " TEXT," +
                                                                TITLE + " TEXT, " +
                                                                " PRIMARY KEY (" + GROUP_KEY + "));";
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

    public void addNewGroup(Group group) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GROUP_KEY, group.getKey());
        values.put(TITLE, group.getTitle());

        // This will insert if record is new, update otherwise
        writableDB.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteGroup(String groupKey) {
        // Define 'where' part of query.
        String selection = GROUP_KEY + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { groupKey };

        // Issue SQL statement.
        writableDB.delete(TABLE_NAME, selection, selectionArgs);
    }

    public Group getGroupByKey(String groupKey) {
        Group group = null;

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = {GROUP_KEY, TITLE};

        // Filter results WHERE GROUP_KEY = groupKey
        String selection = GROUP_KEY + " = ?";
        String[] selectionArgs = {groupKey};

        // Sort the result Cursor
        String sortOrder = TITLE + " DESC";

        Cursor cursor = readableDB.query(
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

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
}