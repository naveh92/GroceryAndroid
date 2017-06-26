package com.example.admin.myapplication.controller.database.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.admin.myapplication.controller.GroceryApp;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;
import com.example.admin.myapplication.model.entities.User;

import java.util.Collections;
import java.util.List;

/**
 * Created by gun2f on 6/17/2017.
 */

public class UserModel {

    private static UserModel instance = null;

    private UsersDB usersDB;
    private UserGroupsDB usersGroupDB;

    private UserModel(){
        usersDB = new UsersDB();
    }

    /***
     * Singletoe func
     * @return
     */
    public static UserModel getInstance() {
        if(instance == null) {
            instance = new UserModel();
        }
        return instance;
    }

    public void findUserByKey(final String userKey, final ObjectReceivedHandler<User> handler){

        //1. get local lastUpdateTade
        SharedPreferences pref = GroceryApp.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE);
        final double lastUpdateDate = pref.getFloat("UsersLastUpdateDate",0);
        Log.d("TAG","lastUpdateDate: " + lastUpdateDate);



        usersDB.findUserByKey(userKey,handler);
    }

    public void findUserByFacebookId(final String facebookId, final ObjectReceivedHandler<User> handler){
        usersDB.findUserByFacebookId(facebookId, handler);
    }

    public void addNewUser(User user){
        usersDB.addNewUser(user);
    }


}
