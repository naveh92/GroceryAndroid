package com.android_project.grocery.controller.database.models;


import com.android_project.grocery.controller.database.remote.UsersDB;
import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.controller.database.local.UsersTable;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;

/**
 * Created by gun2f on 6/17/2017.
 * A User object never changes: Its name, key, etc.
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
        User user = table.getUserByKey(userKey);

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
        User user = table.getUserByFacebookId(facebookId);

        if (user != null) {
            handler.onObjectReceived(user);
        }
        else {
            final ObjectReceivedHandler<User> remoteUserReceivedHandler = new ObjectReceivedHandler<User>() {
                @Override
                public void onObjectReceived(User user) {
                    // If an actual user has been returned
                    if (user != null) {
                        // Save the remote user to the local db.
                        addNewUserToLocal(user);
                    }

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
        table.addNewUser(user);
        // No need to update LastUpdatedTable, because we won't use it.
    }
}
