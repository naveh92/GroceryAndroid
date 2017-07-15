package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.ListsTable;
import com.example.admin.myapplication.controller.database.remote.ListsDB;
import com.example.admin.myapplication.model.entities.GroceryList;

/**
 * Created by gun2f on 6/18/2017.
 *
 * This Model just passes arguments to the DBs.
 */

public class ListsModel {
    private static ListsModel instance;
    // TODO: Should this be here?
    private static ListsTable table;

    private ListsModel() {
        table = new ListsTable();
    }

    public static ListsModel getInstance() {
        if (instance == null) {
            instance = new ListsModel();
        }

        return instance;
    }

    public void addNewList(GroceryList list) {
        // Remote
        ListsDB.getInstance().addNewList(list);

        // Local
        table.addNewList(DatabaseHelper.getInstance().getWritableDatabase(), list);
        // TODO: LastUpdatedTable!! (LISTS)
    }

    public void deleteList(String listKey) {
        // Remote
        ListsDB.getInstance().deleteList(listKey);

        // Local
        table.deleteList(DatabaseHelper.getInstance().getWritableDatabase(), listKey);
        // TODO: LastUpdatedTable!! (LISTS)
    }
}
