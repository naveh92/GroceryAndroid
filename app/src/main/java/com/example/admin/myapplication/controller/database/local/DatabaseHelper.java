package com.example.admin.myapplication.controller.database.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.admin.myapplication.controller.GroceryApp;
import com.example.admin.myapplication.controller.database.models.UserModel;

/**
 * Created by gun2f on 6/17/2017.
 */

/***
 * Schema master
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.db";


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(){
        if(instance == null) {
            instance = new DatabaseHelper(GroceryApp.getMyContext());
        }
        return instance;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        UserGroupsTable.onCreate(db);
        UsersTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        UserGroupsTable.onUpgrade(db, oldVersion, newVersion);
        UsersTable.onUpgrade(db, oldVersion, newVersion);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
