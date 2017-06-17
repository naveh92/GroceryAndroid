package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

import java.util.Map;

/**
 * Created by gun2f on 6/17/2017.
 */

public class UserModel {

    private static UserModel instance = null;

    private UsersDB modelFirebase;

    private UserModel(){
        modelFirebase = new UsersDB();
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
        modelFirebase.findUserByKey(userKey,handler);
    }

    public void findUserByFacebookId(final String facebookId, final ObjectReceivedHandler<User> handler){
        modelFirebase.findUserByFacebookId(facebookId, handler);
    }

    public void addNewUser(User user){
        modelFirebase.addNewUser(user);
    }

}
