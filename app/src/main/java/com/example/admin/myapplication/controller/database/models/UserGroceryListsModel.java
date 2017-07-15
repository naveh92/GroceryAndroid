package com.example.admin.myapplication.controller.database.models;

import android.content.Context;

import com.example.admin.myapplication.controller.database.remote.UserGroceryListsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

import java.util.List;

/**
 * Created by gun2f on 6/18/2017.
 */

public class UserGroceryListsModel {
    private UserGroceryListsDB userGroceryListsDB;

    public UserGroceryListsModel(String userKey){
        userGroceryListsDB = new UserGroceryListsDB(userKey);
    }

    public void observeLists(Context context, final ObjectReceivedHandler<GroceryList> listAdded, final ObjectReceivedHandler<GroceryList> listDeleted) {
        userGroceryListsDB.observeLists(context, listAdded, listDeleted);
    }

    public int getListsCount(){
        return userGroceryListsDB.getListsCount();
    }

    public Boolean doesUserHaveGroup(){
        return userGroceryListsDB.doesUserHaveGroup();
    }

    public GroceryList getGroceryList(int position) {
        return userGroceryListsDB.getGroceryList(position);
    }

    public List<Group> getAllGroups() {
        return userGroceryListsDB.getAllGroups();
    }
}
