package com.android_project.grocery.controller.database.models;

/**
 * Created by admin on 16/07/2017.
 */
public abstract class AbstractModel {
    protected void updateLastUpdatedTable(String tableName, String entityKey) {
        Long updateTime = System.currentTimeMillis();
        LastUpdatedModel.getInstance().setLastUpdateTime(tableName, entityKey, updateTime);
    }
}