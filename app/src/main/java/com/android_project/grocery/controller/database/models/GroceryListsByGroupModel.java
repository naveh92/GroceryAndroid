package com.android_project.grocery.controller.database.models;


import com.android_project.grocery.controller.database.remote.GroceryListsByGroupDB;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;

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
