package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by gun2f on 6/17/2017.
 */

public class UserModel {
    private static UserModel instance;
    private UsersDB usersDB;
    private UserGroupsDB usersGroupDB;

    private UserModel(){
        usersDB = new UsersDB();
    }

    /***
     * Singleton func
     */
    public static UserModel getInstance() {
        if (instance == null) {
            instance = new UserModel();
        }
        return instance;
    }

    public void findUserByKey(final String userKey, final ObjectReceivedHandler<User> handler) {
        usersDB.findUserByKey(userKey,handler);
    }

    public void findUserByFacebookId(final String facebookId, final ObjectReceivedHandler<User> handler){
        usersDB.findUserByFacebookId(facebookId, handler);
    }

    public void addNewUser(User user){
        usersDB.addNewUser(user);
    }
}
