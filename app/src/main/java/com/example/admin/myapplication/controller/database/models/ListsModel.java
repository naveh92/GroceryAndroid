package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.remote.ListsDB;
import com.example.admin.myapplication.model.entities.GroceryList;

import java.util.Map;

/**
 * Created by gun2f on 6/18/2017.
 */

public class ListsModel {

    private static ListsModel instance;


    private ListsModel()
    {

    }

    public static ListsModel getInstance() {
        if (instance == null) {
            instance = new ListsModel();
        }

        return instance;
    }


    public void addNewList(GroceryList list) {
        ListsDB.getInstance().addNewList(list);
    }

    public void deleteList(String listKey) {
        ListsDB.getInstance().deleteList(listKey);
    }
}
