package com.android_project.grocery.controller.database.models;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;
import com.android_project.grocery.controller.database.remote.ListsDB;

/**
 * Created by gun2f on 6/18/2017.
 *
 * This Model just passes arguments to the DBs.
 */
public class ListsModel extends AbstractModel {
    private static ListsModel instance;

    public static ListsModel getInstance() {
        if (instance == null) {
            instance = new ListsModel();
        }

        return instance;
    }

    public void addNewList(final GroceryList list) {
        // Handler for when we receive the key (After we receive the timestamp from server).
        // (No need to store or do anything)
        ObjectReceivedHandler<String> generatedKeyHandler = new ObjectReceivedHandler<String>() {
            @Override
            public void onObjectReceived(String generatedKey) {}
        };

        // Remote - After key is generated, we store in local.
        ListsDB.getInstance().addNewList(list, generatedKeyHandler);
    }

    public void deleteList(String listKey) {
        // Remote
        ListsDB.getInstance().deleteList(listKey);
    }

    /**
     * No need to destroy anything because we don't have any Listeners.
     */
    @Override
    public void destroy() {}
}
