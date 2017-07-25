package com.android_project.grocery.controller;

import android.app.Activity;

import com.android_project.grocery.controller.database.local.DatabaseHelper;

/**
 * Created by Naveh on 25/7/2017.
 */

public abstract class CustomActivity extends Activity {
    @Override
    protected void onDestroy() {
        DatabaseHelper.getInstance().close();
        super.onDestroy();
    }
}
