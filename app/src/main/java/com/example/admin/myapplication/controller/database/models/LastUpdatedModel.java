package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.LastUpdatedTable;

/**
 * Created by admin on 16/07/2017.
 *
 * This Model manages the cache-relevance and saves the date in which every Table was last updated.
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

    public void setLastUpdateTime(String tableName, String entityKey, Long updateTime) {
        table.setLastUpdateTime(DatabaseHelper.getInstance().getWritableDatabase(), tableName, entityKey, updateTime);
    }
    public Long getLastUpdateTime(String tableName, String entityKey) {
        return table.getLastUpdateTime(DatabaseHelper.getInstance().getReadableDatabase(), tableName, entityKey);
    }
    public void releaseCache() {
        table.truncate(DatabaseHelper.getInstance().getWritableDatabase());
    }
}