package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.RequestsTable;
import com.example.admin.myapplication.controller.database.remote.RequestsDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryRequest;

import java.util.List;

/**
 * Created by gun2f on 6/18/2017.
 *
 * This Model fetches the local updateTime, and then fetches remote records that have a higher updateTime than it.
 * Also fetches all local records.
 */
public class RequestsModel extends AbstractModel {
    private RequestsDB requestsDB;
    private static RequestsTable table;
    private String listKey;

    public RequestsModel(String listKey) {
        this.listKey = listKey;
        requestsDB = new RequestsDB(listKey);

        if (table == null) {
            table = new RequestsTable();
        }
    }

    public void observeRequestsAddition(final ObjectHandler<GroceryRequest> handler) {
        // Get the last-update time from the local db
        Long localUpdateTime = LastUpdatedModel.getInstance().getLastUpdateTime(table.getTableName(), listKey);
        // Reset the objects, we are about to get a new set.

        handler.removeAllObjects();

        // Create a handler to handle all incoming requests from remote db.
        final ObjectHandler<GroceryRequest> requestFromRemoteHandler = new ObjectHandler<GroceryRequest>() {
            @Override
            public void onObjectReceived(GroceryRequest request) {
                // Save the received request to the local storage.
                addRequestToLocal(request);

                // Pass the request to the handler.
                handler.onObjectReceived(request);
            }

            @Override
            public void removeAllObjects() {
                // Pass the message on.
                handler.removeAllObjects();
            }
        };

        if (localUpdateTime != null && localUpdateTime != 0L) {
            // -----------------------------
            //       Query observation
            // -----------------------------

            // Retrieve from local DB before remote DB.
            // Get the old Groups from local.
            fetchRequestsFromLocal(handler);

            // Observe only if the remote update-time is after the the local
            requestsDB.observeRequestsByLastUpdateDate(localUpdateTime, requestFromRemoteHandler);
        }
        else {
            // ------------------------------
            //      Regular observation
            // ------------------------------

            // Note: Every time we get an update from remote, the new request will replace the old one.
            // We handle updates, and not just new requests.

            // Observe all group records from remote group node
            requestsDB.observeAllRequests(requestFromRemoteHandler);
        }
    }

    private void addRequestToLocal(GroceryRequest request) {
        table.addNewRequest(listKey, request);
        updateLastUpdateTime();
    }

    private void fetchRequestsFromLocal(ObjectHandler<GroceryRequest> handler) {
        // Get the requests from local db
        List<GroceryRequest> requestsFromLocal = table.getRequestsByListKey(listKey);

        // Handle each request individually
        for (GroceryRequest r : requestsFromLocal) {
            handler.onObjectReceived(r);
        }
    }

    public void addNewRequest(final GroceryRequest request) {
        ObjectReceivedHandler<String> generatedKeyHandler = new ObjectReceivedHandler<String>() {
            @Override
            public void onObjectReceived(String generatedKey) {
                request.setKey(generatedKey);

                // Local
                addRequestToLocal(request);
            }
        };

        // Remote
        requestsDB.addNewRequest(request, generatedKeyHandler);
    }

    public void togglePurchased(final String requestKey, final Boolean currentPurchasedValue) {
        // Remote
        requestsDB.togglePurchased(requestKey, currentPurchasedValue);

        // Local
        table.togglePurchased(requestKey, currentPurchasedValue);
        updateLastUpdateTime();
    }

    public void updateItemName(final String requestKey, final String newItemName) {
        requestsDB.updateItemName(requestKey, newItemName);

        // Local
        table.updateItemName(requestKey, newItemName);
        updateLastUpdateTime();
    }

    /**
     * Local DB Functions
     */
    private void updateLastUpdateTime() {
        updateLastUpdatedTable(table.getTableName(), listKey);
    }
}
