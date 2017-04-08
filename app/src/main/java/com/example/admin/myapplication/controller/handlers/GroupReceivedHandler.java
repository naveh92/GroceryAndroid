package com.example.admin.myapplication.controller.handlers;

import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 08/04/2017.
 */
public interface GroupReceivedHandler {
    void onGroupReceived(Group group);
    void removeAllGroups();
}
