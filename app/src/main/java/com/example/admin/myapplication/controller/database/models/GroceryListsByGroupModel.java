package com.example.admin.myapplication.controller.database.models;


import com.example.admin.myapplication.controller.database.local.ListsTable;
import com.example.admin.myapplication.controller.database.remote.GroceryListsByGroupDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;

/**
 * Created by admin on 18/07/2017.
 */
public class GroceryListsByGroupModel extends AbstractModel {
    private GroceryListsByGroupDB listsDb;
//    private ListsTable table;
    private String groupKey;

    public GroceryListsByGroupModel(String groupKey) {
        this.groupKey = groupKey;
        listsDb = new GroceryListsByGroupDB(groupKey);
//        table = new ListsTable();
    }

    public void observeLists(final ObjectReceivedHandler<GroceryList> listAddedHandler,
                             final ObjectReceivedHandler<GroceryList> listRemovedHandler) {
//        // Get the last-update time from the local db
//        Long localUpdateTime = LastUpdatedModel.getInstance().getLastUpdateTime(table.getTableName(), groupKey);
//
//        // TODO:
////        // Reset the array of. We are about to get a new array.
////        groups.clear();
//
//        if (localUpdateTime != null && localUpdateTime != 0L) {
//            // -----------------------------
//            //       Query observation
//            // -----------------------------
//            // TODO: Need to merge if a list was deleted from remote..
//
//            // Retrieve from local DB before remote DB.
//            // Get the old Lists from local.
//            fetchListsFromLocal(listAddedHandler);
//
//            // Observe only if the remote update-time is after the the local
//            listsDb.observeListsByLastUpdateTime(localUpdateTime, listAddedHandler, listRemovedHandler);
//        }
//        else {
//            // -------------------------------
//            // Handler for regular observation
//            // -------------------------------
//            // TODO: Need to merge if a list was deleted from remote..
//
//            // Observe all list records from remote group-node (filtered)
//            listsDb.observeLists(listAddedHandler, listRemovedHandler);
//        }

        // NOTE: We cannot create a query to fetch by updateTime, because Firebase doesn't allow 2 filters.
        // Observe all list records from remote group-node (filtered)
        listsDb.observeLists(listAddedHandler, listRemovedHandler);
    }

//    private void fetchListsFromLocal(ObjectReceivedHandler<GroceryList> listAddedHandler) {
//        // Fetch from local
//        List<GroceryList> lists = table.getListsByGroupKey(DatabaseHelper.getInstance().getReadableDatabase(), groupKey);
//
//        // Handle each received list individually
//        for (GroceryList list : lists) {
//            listAddedHandler.onObjectReceived(list);
//        }
//    }

    /**
     * -------------------
     *  Utility functions
     * -------------------
     */
    public String getGroupKey() {
        return groupKey;
    }
}
