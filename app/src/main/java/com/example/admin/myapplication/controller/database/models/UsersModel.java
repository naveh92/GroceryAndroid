package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.UsersTable;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by gun2f on 6/17/2017.
 *
 * This Model tries to get the User from local DB first,
 * and if it doesn't exist, fetches it from Remote DB and adds it to local.
 */
public class UsersModel extends AbstractModel {
    private static UsersModel instance;
    private UsersDB usersDB;
    private static UsersTable table;

    private UsersModel() {
        usersDB = new UsersDB();
        table = new UsersTable();
    }

    /**
     * Singleton instance
     */
    public static UsersModel getInstance() {
        if (instance == null) {
            instance = new UsersModel();
        }
        return instance;
    }

    public void findUserByKey(final String userKey, final ObjectReceivedHandler<User> handler) {
        // Try to get the user from the local DB.
        User user = table.getUserByKey(DatabaseHelper.getInstance().getReadableDatabase(), userKey);

        if (user != null) {
            handler.onObjectReceived(user);
        }
        else {
            final ObjectReceivedHandler<User> remoteUserReceivedHandler = new ObjectReceivedHandler<User>() {
                @Override
                public void onObjectReceived(User user) {
                    if (user != null) {
                        // Save the remote user to the local db.
                        addNewUserToLocal(user);
                    }

                    // Pass it on.
                    handler.onObjectReceived(user);
                }
            };

            // If localDB failed, try to fetch from remote.
            usersDB.findUserByKey(userKey, remoteUserReceivedHandler);
        }
    }

    public void findUserByFacebookId(final String facebookId, final ObjectReceivedHandler<User> handler) {
        // Try to get the user from the local DB.
        User user = table.getUserByFacebookId(DatabaseHelper.getInstance().getReadableDatabase(), facebookId);

        if (user != null) {
            handler.onObjectReceived(user);
        }
        else {
            final ObjectReceivedHandler<User> remoteUserReceivedHandler = new ObjectReceivedHandler<User>() {
                @Override
                public void onObjectReceived(User user) {
                    // Save the remote user to the local db.
                    addNewUserToLocal(user);

                    // Pass it on.
                    handler.onObjectReceived(user);
                }
            };

            // If localDB failed, try to fetch from remote.
            usersDB.findUserByFacebookId(facebookId, remoteUserReceivedHandler);
        }
    }

    public void addNewUser(User user) {
        // Add to local
        addNewUserToLocal(user);

        // Add to remote
        usersDB.addNewUser(user);
    }

    private void addNewUserToLocal(User user) {
        table.addNewUser(DatabaseHelper.getInstance().getWritableDatabase(), user);
//        TODO: LastUpdatedTable? We don't really use this..
    }
}
