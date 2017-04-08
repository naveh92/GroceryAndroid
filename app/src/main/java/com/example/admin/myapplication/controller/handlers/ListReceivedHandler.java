package com.example.admin.myapplication.controller.handlers;

import com.example.admin.myapplication.model.entities.GroceryList;

/**
 * Created by admin on 08/04/2017.
 */
public interface ListReceivedHandler {
    void onListReceived(GroceryList list);
    void removeAllLists();
}
