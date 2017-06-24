package com.example.admin.myapplication.controller.database.local;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.admin.myapplication.model.entities.User;

/**
 * Created by gun2f on 6/24/2017.
 */

public class UsersTable {

    private static final String TABLE_NAME = "USERS";
    private static final String USER_KEY = "USER_KEY";
    private static final String FACEBOOK_KEY = "FACEBOOK_KEY";
    private static final String NAME = "NAME";


    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + USER_KEY + " TEXT," +
                    "                                       " + FACEBOOK_KEY + " TEXT," +
                    "                                       " + NAME + " TEXT);";
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

    public void addNewUser(SQLiteDatabase db, User user) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(USER_KEY, user.getKey());
        values.put(FACEBOOK_KEY, user.getFacebookId());
        values.put(NAME, user.getName());

        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_NAME, null, values);

    }
}