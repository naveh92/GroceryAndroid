package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by gun2f on 6/19/2017.
 */

public class GroupMembersModel {

    private GroupMembersDB groupMembersDB;

    public GroupMembersModel(String groupKey){

        groupMembersDB = new GroupMembersDB(groupKey);
    }

    public void observeGroupMembers(final ObjectHandler<User> handler){
        groupMembersDB.observeGroupMembers(handler);
    }

    public void addMember(String userKey){
        groupMembersDB.addMember(userKey);
    }

    public void removeMember(String userKey){
        groupMembersDB.removeMember(userKey);
    }

    public int getMembersCount(){
        return groupMembersDB.getMembersCount();
    }

    public User getMember(int position){
        return groupMembersDB.getMember(position);
    }
}
