package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.LastUpdatedTable;

/**
 * Created by admin on 16/07/2017.
 */
public class LastUpdatedModel {
    private static LastUpdatedModel instance;
    private static LastUpdatedTable table;

    private LastUpdatedModel() {
        table = new LastUpdatedTable();
    }

    public static LastUpdatedModel getInstance() {
        if (instance == null) {
            instance = new LastUpdatedModel();
        }
        return instance;
    }

    public void setLastUpdateTime(String tableName, Long updateTime) {
        table.setLastUpdateTime(DatabaseHelper.getInstance().getWritableDatabase(), tableName, updateTime);
    }
    public Long getLastUpdateTime(String tableName) {
        return table.getLastUpdateTime(DatabaseHelper.getInstance().getReadableDatabase(), tableName);
    }
    public void releaseCache() {
        table.truncate(DatabaseHelper.getInstance().getWritableDatabase());
    }
}