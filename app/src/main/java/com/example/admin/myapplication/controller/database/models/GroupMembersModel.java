package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.GroupMembersTable;
import com.example.admin.myapplication.controller.database.remote.GroceryListsByGroupDB;
import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gun2f on 6/19/2017.
 *
 * This Model's execution:
 * Fetch the RemoteUpdateTime (Every Group has it's own).
 * If it is after the localUpdateTime, fetch all records from remote DB, and save to local DB.
 * If it is before localUpdateTime, fetch all records from local DB.
 */

public class GroupMembersModel extends AbstractModel {
    private final GroupMembersDB groupMembersDB;
    // TODO: Need to table.close() onDestroy()??
    private static GroupMembersTable table;
    private final String groupKey;

    // Data-models
    private final List<User> members = new ArrayList<>();

    public GroupMembersModel(String groupKey) {
        this.groupKey = groupKey;
        groupMembersDB = new GroupMembersDB(groupKey);

        // Make sure the local db is initialized
        if (table == null) {
            table = new GroupMembersTable();
        }
    }

    public void addMember(String userKey) {
        // Local
        table.insert(DatabaseHelper.getInstance().getWritableDatabase(), groupKey, userKey);
        updateLastUpdateTime();

        // Remote
        groupMembersDB.addMember(userKey);
    }

    public void removeMember(String userKey) {
        // Local
        table.delete(DatabaseHelper.getInstance().getWritableDatabase(), groupKey, userKey);
        updateLastUpdateTime();

        // Remote
        groupMembersDB.removeMember(userKey);

        deleteGroupIfEmpty();
    }

    private void deleteGroupIfEmpty() {
        ObjectReceivedHandler<Integer> membersCountHandler = new ObjectReceivedHandler<Integer>() {
            @Override
            public void onObjectReceived(Integer count) {
                // If there are no more members in the group
                if (count == 0) {
                    // Only call other Models instead of other DBs

                    // Delete the group
                    GroupsModel.getInstance().deleteGroup(groupKey);

                    // Delete all lists in that group
                    new GroceryListsByGroupModel().deleteAllListsForGroup(groupKey);
                }
            }
        };

        // Fetch the group members count from the Remote DB.
        groupMembersDB.findGroupMembersCount(membersCountHandler);
    }

    /**
     * ---------------------
     *  Remote DB Functions
     * ---------------------
     */

    /**
     * This function fetches from Local (If up-to-date) or from Remote (If not up-to-date).
     */
    public void observeGroupMembers(final ObjectHandler<User> handler) {
        final ObjectReceivedHandler<Long> remoteUpdateTimeHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long remoteLastUpdateTime) {
                // Clear the list - we are about to get a new value.
                members.clear();
                handler.removeAllObjects();

                // Check if we need to update.
                if (isLocalDatabaseUpToDate(remoteLastUpdateTime)) {
                    // We don't need to update, fetch only from Local.

                    List<String> userKeysFromLocal = fetchGroupMembersFromLocalDB();
                    handleGroupMembersFromLocal(userKeysFromLocal, handler);
                }
                else {
                    // We need to update, fetch from remote and merge with local.

                    ObjectReceivedHandler<List<String>> remoteKeysHandler = new ObjectReceivedHandler<List<String>>() {
                        @Override
                        public void onObjectReceived(List<String> userKeys) {

                            // TODO: Shouldn't ALL the clears be BEFORE the query? Ex: If no userKeys return from the remote, this function will not be called..
                            // Clear the list - we are about to get a new value.
                            members.clear();
                            handler.removeAllObjects();

                            // Handle the received group members.
                            handleGroupMembers(userKeys, handler);
                        }
                    };

                    // Fetch the keys from remote.
                    groupMembersDB.fetchGroupMembers(remoteKeysHandler);
                }
            }
        };

        // Fetch the value of the LastUpdatedTime from the remote.
        groupMembersDB.getLastUpdatedTime(remoteUpdateTimeHandler);
    }

    /**
     * ----------------------
     *   Local DB Functions
     * ----------------------
     */

    private void updateLastUpdateTime() {
        updateLastUpdatedTable(table.getTableName(), groupKey);
    }

    private boolean isLocalDatabaseUpToDate(Long remoteLastUpdateDate) {
        // Get the lastUpdateTime for this table from the LastUpdateTable in local DB.
        Long localLastUpdateDate = LastUpdatedModel.getInstance().getLastUpdateTime(table.getTableName(), groupKey);
        return localLastUpdateDate >= remoteLastUpdateDate;
    }

    private List<String> fetchGroupMembersFromLocalDB() {
        return table.getGroupMembers(DatabaseHelper.getInstance().getReadableDatabase(), groupKey);
    }

    /**
     * ---------------------
     *    Side-Functions
     * ---------------------
     */

    private void handleGroupMembers(List<String> userKeys, final ObjectHandler<User> handler) {
        for (String userKey : userKeys) {
            // Handle each member fetched independently
            handleGroupMemberAddition(userKey, handler);
        }

        // Update local records.
        // Don't truncate, only delete all group members for this groupKey
        table.deleteAllGroupMembers(DatabaseHelper.getInstance().getWritableDatabase(), groupKey);
        table.insertGroupMembers(DatabaseHelper.getInstance().getWritableDatabase(), groupKey, userKeys);

        updateLastUpdateTime();
    }

    private void handleGroupMembersFromLocal(List<String> userKeys, final ObjectHandler<User> handler) {
        for (String userKey : userKeys) {
            // Handle each member fetched independently
            handleGroupMemberAddition(userKey, handler);
        }
    }

    private void handleGroupMemberAddition(String userKey, final ObjectReceivedHandler<User> handler) {
        ObjectReceivedHandler<User> foundUserHandler = new ObjectReceivedHandler<User>() {
            @Override
            public void onObjectReceived(User user) {
                if (user != null && !members.contains(user)) {
                    members.add(user);
                    handler.onObjectReceived(user);
                }
            }
        };

        // Retrieve the user object
        UsersModel.getInstance().findUserByKey(userKey, foundUserHandler);
    }

    /**
     * ---------------------
     *  Container-Functions
     * ---------------------
     */
    public int getMembersCount() {
        return members.size();
    }
    public User getMember(int position) {
        if (position < getMembersCount()) {
            return members.get(position);
        }

        return null;
    }

    /**
     * This inner-class is only needed for deleting all lists for a group when the group is deleted.
     * It was created to avoid GroupMembersModel calling GroceryListsByGroupDB directly.
     */
    private class GroceryListsByGroupModel {
        private void deleteAllListsForGroup(String groupKey) {
            GroceryListsByGroupDB.deleteAllListsForGroup(groupKey);
        }
    }
}
