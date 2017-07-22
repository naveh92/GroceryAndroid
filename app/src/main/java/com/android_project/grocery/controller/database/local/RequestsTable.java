package com.android_project.grocery.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android_project.grocery.model.entities.GroceryRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 6/24/2017.
 */
public class RequestsTable extends AbstractTable {
    private static final String TABLE_NAME = "REQUESTS";
    private static final String REQUEST_KEY = "REQUEST_KEY";
    private static final String LIST_KEY = "LIST_KEY";
    private static final String USER_KEY = "USER_KEY";
    private static final String ITEM_NAME = "ITEM_NAME";
    private static final String PURCHASED = "PURCHASED";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + REQUEST_KEY + " TEXT," +
                                                                LIST_KEY + " TEXT," +
                                                                USER_KEY + " TEXT," +
                                                                ITEM_NAME + " TEXT," +
                                                                PURCHASED + " INTEGER, " +
                                                                " PRIMARY KEY (" + REQUEST_KEY + "));";
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

    public void addNewRequest(String listKey, GroceryRequest request) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(REQUEST_KEY, request.getKey());
        values.put(LIST_KEY, listKey);
        values.put(USER_KEY, request.getUserKey());
        values.put(ITEM_NAME, request.getItemName());
        values.put(PURCHASED, boolToInt(request.getPurchased()));

        // Insert the new row, returning the primary key value of the new row
        // This will insert if record is new, update otherwise
        writableDB.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void togglePurchased(String requestKey, Boolean currentValue) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PURCHASED, boolToInt(!currentValue));

        // Filter results WHERE GROUP_KEY = groupKey
        String selection = REQUEST_KEY + " = ?";
        String[] selectionArgs = {requestKey};

        // Update the existing row
        writableDB.update(TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateItemName(String requestKey, String newItemName) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, newItemName);

        // Filter results WHERE GROUP_KEY = groupKey
        String selection = REQUEST_KEY + " = ?";
        String[] selectionArgs = {requestKey};

        // Update the existing row
        writableDB.update(TABLE_NAME, values, selection, selectionArgs);
    }

    public List<GroceryRequest> getRequestsByListKey(String listKey) {
        List<GroceryRequest> requests = new ArrayList<>();

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = {REQUEST_KEY, USER_KEY, ITEM_NAME, PURCHASED};

        // Filter results WHERE LIST_KEY = listKey
        String selection = LIST_KEY + " = ?";
        String[] selectionArgs = {listKey};

        // Sort the result Cursor
        String sortOrder = ITEM_NAME + " DESC";

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
        while (cursor.moveToNext()) {
            // Extract the data
            String key = cursor.getString(cursor.getColumnIndexOrThrow(REQUEST_KEY));
            String userKey = cursor.getString(cursor.getColumnIndexOrThrow(USER_KEY));
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ITEM_NAME));
            Boolean purchased = intToBool(cursor.getInt(cursor.getColumnIndexOrThrow(PURCHASED)));

            // Create a new GroceryRequest and add it.
            GroceryRequest currentRequest = new GroceryRequest(key, userKey, itemName, purchased);
            requests.add(currentRequest);
        }
        cursor.close();

        return requests;
    }

    /**
     * Util casting functions
     */
    private Boolean intToBool(int num) {
        return num != 0;
    }
    private Integer boolToInt(boolean b) {
        return b? 1:0;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
}
