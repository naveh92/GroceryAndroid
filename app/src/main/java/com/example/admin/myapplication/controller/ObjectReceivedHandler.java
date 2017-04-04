package com.example.admin.myapplication.controller;

/**
 * Created by admin on 04/04/2017.
 */
public interface ObjectReceivedHandler {
    void onObjectReceived(Object obj);
    void removeAllObjects();
}
