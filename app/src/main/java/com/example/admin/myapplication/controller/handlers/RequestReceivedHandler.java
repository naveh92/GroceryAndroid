package com.example.admin.myapplication.controller.handlers;

import com.example.admin.myapplication.model.entities.GroceryRequest;

/**
 * Created by admin on 08/04/2017.
 */
public interface RequestReceivedHandler {
    void onRequestReceived(GroceryRequest request);
    void removeAllRequests();
}
