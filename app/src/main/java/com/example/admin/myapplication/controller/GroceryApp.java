package com.example.admin.myapplication.controller;

import android.app.Application;
import android.content.Context;

/**
 * Created by gun2f on 6/24/2017.
 */

public class GroceryApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


    public static Context getMyContext(){
        return context;
    }

}
