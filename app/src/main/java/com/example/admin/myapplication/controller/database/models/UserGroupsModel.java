package com.example.admin.myapplication.controller.database.models;

import android.content.Context;

import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;

import java.util.List;

/**
 * Created by gun2f on 6/18/2017.
 */

public class UserGroupsModel {

    private UserGroupsDB usersGroupDB;

    public UserGroupsModel(String userKey){
        usersGroupDB = new UserGroupsDB(userKey);
    }

    public void observeUserGroupsAddition( Context context, final ObjectReceivedHandler<Group> handler){



        usersGroupDB.observeUserGroupsAddition(context, handler);
    }

    public int getGroupsCount() {


        return usersGroupDB.getGroupsCount();
    }

    public Group getGroup(int position) {


        return usersGroupDB.getGroup(position);
    }

    public List<Group> getAllGroups() {


        return usersGroupDB.getAllGroups();
    }

    public void addGroupToUser(String groupKey) {


        usersGroupDB.addGroupToUser(groupKey);
    }

    public void removeGroupFromUser(final String groupKey){
        usersGroupDB.removeGroupFromUser(groupKey);
    }
}
