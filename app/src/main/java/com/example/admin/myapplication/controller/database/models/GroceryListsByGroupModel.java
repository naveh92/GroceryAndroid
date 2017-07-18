package com.example.admin.myapplication.controller.database.models;


import com.example.admin.myapplication.controller.database.remote.GroceryListsByGroupDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;

/**
 * Created by admin on 18/07/2017.
 *
 * This Model passes arguments to the GroceryListsByGroupDB.
 * NOTE: We cannot create a query to fetch by updateTime, because Firebase doesn't allow 2 filters.
 */
public class GroceryListsByGroupModel extends AbstractModel {
    private GroceryListsByGroupDB listsDb;
    private String groupKey;

    public GroceryListsByGroupModel(String groupKey) {
        this.groupKey = groupKey;
        listsDb = new GroceryListsByGroupDB(groupKey);
    }

    public void observeLists(final ObjectReceivedHandler<GroceryList> listAddedHandler,
                             final ObjectReceivedHandler<GroceryList> listRemovedHandler) {
        // NOTE: We cannot create a query to fetch by updateTime, because Firebase doesn't allow 2 filters.
        // Observe all list records from remote group-node (filtered)
        listsDb.observeLists(listAddedHandler, listRemovedHandler);
    }

    /**
     * -------------------
     *  Utility functions
     * -------------------
     */
    public String getGroupKey() {
        return groupKey;
    }
}
