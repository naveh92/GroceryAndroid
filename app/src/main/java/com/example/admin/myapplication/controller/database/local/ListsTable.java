package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.admin.myapplication.model.entities.GroceryList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 6/24/2017.
 */

public class ListsTable extends AbstractTable {
    private static final String TABLE_NAME = "LISTS";
    private static final String LIST_KEY = "LIST_KEY";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final String TITLE = "TITLE";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + LIST_KEY + " TEXT," +
                    "                                       " + GROUP_KEY + " TEXT," +
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

    public void addNewList(SQLiteDatabase db, GroceryList list) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LIST_KEY, list.getKey());
        values.put(GROUP_KEY, list.getGroupKey());
        values.put(TITLE, list.getTitle());

        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_NAME, null, values);
    }

    public void deleteList(SQLiteDatabase db, String listKey) {
        // Define 'where' part of query.
        String selection = LIST_KEY + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { listKey };

        // Issue SQL statement.
        db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public void deleteAllListsForGroup(SQLiteDatabase db,String groupKey) {
        // Define 'where' part of query.
        String selection = GROUP_KEY + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { groupKey };

        // Issue SQL statement.
        db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public List<GroceryList> getListsByGroupKey(SQLiteDatabase db, String groupKey) {
        List<GroceryList> lists = new ArrayList<>();

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = {LIST_KEY, GROUP_KEY, TITLE};

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
            // Extract the data
            String key = cursor.getString(cursor.getColumnIndexOrThrow(LIST_KEY));
            String group_Key = cursor.getString(cursor.getColumnIndexOrThrow(GROUP_KEY));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));

            // Create a new GroceryList and add it.
            GroceryList currentList = new GroceryList(key, group_Key, title);
            lists.add(currentList);
        }
        cursor.close();

        return lists;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
}
