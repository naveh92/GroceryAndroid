package com.example.admin.myapplication.controller.handlers;

import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 08/04/2017.
 */
public interface UserReceivedHandler {
    void onUserReceived(User user);
    void removeAllUsers();
}
