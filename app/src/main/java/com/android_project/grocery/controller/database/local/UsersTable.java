package com.android_project.grocery.controller.database.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android_project.grocery.model.entities.User;

/**
 * Created by admin on 6/24/2017.
 */
public class UsersTable extends AbstractTable {
    private static final String TABLE_NAME = "USERS";
    private static final String USER_KEY = "USER_KEY";
    private static final String FACEBOOK_KEY = "FACEBOOK_KEY";
    private static final String NAME = "NAME";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + USER_KEY + " TEXT," +
                                                                FACEBOOK_KEY + " TEXT," +
                                                                NAME + " TEXT, " +
                                                                " PRIMARY KEY (" + USER_KEY + "));";
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

    public void addNewUser(User user) {
        final SQLiteDatabase writableDB = getWritableDB();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(USER_KEY, user.getKey());
        values.put(FACEBOOK_KEY, user.getFacebookId());
        values.put(NAME, user.getName());

        // Insert the new row, returning the primary key value of the new row
        // This will insert if record is new, update otherwise
        writableDB.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public User getUserByKey(String userKey) {
        final SQLiteDatabase readableDB = getReadableDB();
        User user = null;

        if (userKey != null) {
            // Define a projection that specifies which columns from the database
            // we will actually use after this query.
            String[] projection = {USER_KEY, FACEBOOK_KEY, NAME};

            // Filter results WHERE USER_KEY = userKey
            String selection = USER_KEY + " = ?";
            String[] selectionArgs = {userKey};

            // Sort the result Cursor
            String sortOrder = NAME + " DESC";

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
                String key = cursor.getString(cursor.getColumnIndexOrThrow(USER_KEY));
                String facebookId = cursor.getString(cursor.getColumnIndexOrThrow(FACEBOOK_KEY));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

                user = new User(key, facebookId, name);
            }
            cursor.close();
        }

        return user;
    }

    public User getUserByFacebookId(String facebookId) {
        final SQLiteDatabase readableDB = getReadableDB();
        User user = null;

        // Define a projection that specifies which columns from the database
        // we will actually use after this query.
        String[] projection = { USER_KEY, FACEBOOK_KEY, NAME };

        // Filter results WHERE USER_KEY = userKey
        String selection = FACEBOOK_KEY + " = ?";
        String[] selectionArgs = { facebookId };

        // Sort the result Cursor
        String sortOrder = NAME + " DESC";

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
            String key = cursor.getString(cursor.getColumnIndexOrThrow(USER_KEY));
            String fbId = cursor.getString(cursor.getColumnIndexOrThrow(FACEBOOK_KEY));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

            user = new User(key, fbId, name);
        }
        cursor.close();

        return user;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
}