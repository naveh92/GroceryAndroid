package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.GroupMembersTable;
import com.example.admin.myapplication.controller.database.remote.GroceryListsByGroupDB;
import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gun2f on 6/19/2017.
 */

public class GroupMembersModel {
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
        groupMembersDB.addMember(userKey);

        // TODO:
//        table.insert(???, groupKey, userKey);
        updateLastUpdatedTable();
    }

    public void removeMember(String userKey) {
        groupMembersDB.removeMember(userKey);

        // TODO:
//        table.delete(???, groupKey, userKey);
        updateLastUpdatedTable();

        deleteGroupIfEmpty();
    }

    private void deleteGroupIfEmpty() {
        ObjectReceivedHandler<Integer> membersCountHandler = new ObjectReceivedHandler<Integer>() {
            @Override
            public void onObjectReceived(Integer count) {
                // If there are no more members in the group
                if (count == 0) {
                    // TODO: only call other Models instead of other DBs?

                    // Delete the group
                    GroupsDB.getInstance().deleteGroup(groupKey);

                    // Delete all lists in that group
                    new GroceryListsByGroupDB(groupKey).deleteAllListsForGroup();
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
                // Check if we need to update.
                if (isLocalDatabaseUpToDate(remoteLastUpdateTime)) {
                    // We don't need to update, fetch only from Local.
                    List<String> userKeysFromLocal = fetchGroupMembersFromLocalDB();
                    handleGroupMembers(userKeysFromLocal, handler);
                }
                else {
                    // We need to update, fetch from remote and merge with local.

                    // Clear the list - we are about to get a new value.
                    members.clear();
                    handler.removeAllObjects();

                    ObjectReceivedHandler<List<String>> remoteKeysHandler = new ObjectReceivedHandler<List<String>>() {
                        @Override
                        public void onObjectReceived(List<String> userKeys) {
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

    private boolean isLocalDatabaseUpToDate(long remoteLastUpdateDate) {
        // TODO:
        Long localLastUpdateDate = 0l; // LastUpdateTable....
        return localLastUpdateDate >= remoteLastUpdateDate;
    }

    public List<String> fetchGroupMembersFromLocalDB() {
        // TODO:
//        return table.getGroupMembers(???, groupKey);
        return new ArrayList<>();
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
        table.truncate(DatabaseHelper.getInstance().getWritableDatabase());
        table.insertGroupMembers(DatabaseHelper.getInstance().getWritableDatabase(), groupKey, userKeys);

        updateLastUpdatedTable();
    }

    private void updateLastUpdatedTable() {
        // TODO: Update LastUpdateTable
//        LastUpdateTable.setLastUpdate(database: LocalDb.sharedInstance?.database,
//                table: GroupMembersTable.TABLE,
//                key: self.groupKey as String,
//                lastUpdate: Date())
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
        UserModel.getInstance().findUserByKey(userKey, foundUserHandler);
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
}
