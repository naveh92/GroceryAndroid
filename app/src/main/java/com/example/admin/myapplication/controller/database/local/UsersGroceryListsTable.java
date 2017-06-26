package com.example.admin.myapplication.controller.database.local;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by gun2f on 6/24/2017.
 */

public class UsersGroceryListsTable {

    private static final String TABLE_NAME = "USERS_GROCERY_LIST";
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
}
