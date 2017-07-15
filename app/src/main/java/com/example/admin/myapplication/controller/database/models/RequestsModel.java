package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.remote.RequestsDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.model.entities.GroceryRequest;

/**
 * Created by gun2f on 6/18/2017.
 */

public class RequestsModel {
    private RequestsDB requestsDB;
    public RequestsModel(String listKey){
        requestsDB = new RequestsDB(listKey);
    }

    public void observeRequestsAddition(final ObjectHandler<GroceryRequest> handler) {
        requestsDB.observeRequestsAddition(handler);
    }

    public void addNewRequest(final GroceryRequest request) {
        requestsDB.addNewRequest(request);
    }

    public void togglePurchased(final String requestKey, final Boolean currentPurchasedValue) {
        requestsDB.togglePurchased(requestKey, currentPurchasedValue);
    }

    public void updateItemName(final String requestKey, final String newItemName) {
        requestsDB.updateItemName(requestKey, newItemName);
    }
}
