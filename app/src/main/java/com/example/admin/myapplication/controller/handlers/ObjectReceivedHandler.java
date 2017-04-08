package com.example.admin.myapplication.controller.handlers;

/**
 * Created by admin on 08/04/2017.
 */
public interface ObjectReceivedHandler<T> {
    void onObjectReceived(T obj);
    void removeAllObjects();
}
