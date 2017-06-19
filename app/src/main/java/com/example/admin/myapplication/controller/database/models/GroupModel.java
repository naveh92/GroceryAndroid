package com.example.admin.myapplication.controller.database.models;

import android.util.Log;

import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by gun2f on 6/19/2017.
 */

public class GroupModel {

    private static GroupModel instance;

    private GroupModel() {

    }
    public static GroupModel getInstance() {
        if (instance == null) {
            instance = new GroupModel();
        }

        return instance;
    }

    public String addNewGroup(Group group) {
        return GroupsDB.getInstance().addNewGroup(group);
    }

    public void deleteGroup(String key){
        GroupsDB.getInstance().deleteGroup(key);

    }

    public void findGroupByKey(String key, final ObjectReceivedHandler<Group> handler) {
        GroupsDB.getInstance().findGroupByKey(key, handler);

    }
}
