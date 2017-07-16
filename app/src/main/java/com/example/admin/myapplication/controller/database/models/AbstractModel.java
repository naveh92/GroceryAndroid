package com.example.admin.myapplication.controller.database.models;

/**
 * Created by admin on 16/07/2017.
 */
public abstract class AbstractModel {
    protected void updateLastUpdatedTable(String tableName) {
        Long updateTime = System.currentTimeMillis();
        LastUpdatedModel.getInstance().setLastUpdateTime(tableName, updateTime);
    }
}
