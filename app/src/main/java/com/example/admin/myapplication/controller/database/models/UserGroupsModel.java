package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.UserGroupsTable;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gun2f on 6/18/2017.
 *
 * This Model fetches the lastUpdatedTime from local, and fetches the records that were updated after that time from remote DB.
 * It then fetches all other records from local DB, merges the data, and saves to local DB.
 */
public class UserGroupsModel extends AbstractModel {
    private final String userKey;
    private UserGroupsDB usersGroupDB;
    // TODO: Need to table.close() onDestroy()??
    private static UserGroupsTable table;

    // Data-models
    private final List<Group> groups = new ArrayList<>();

    public UserGroupsModel(String userKey) {
        this.userKey = userKey;
        this.usersGroupDB = new UserGroupsDB(userKey);

        // Make sure the local db is initialized
        if (table == null) {
            table = new UserGroupsTable();
        }
    }

    public void addGroupToUser(String groupKey) {
        // Remote
        usersGroupDB.addGroupToUser(groupKey);

        // Local
        table.insert(DatabaseHelper.getInstance().getWritableDatabase(), userKey, groupKey);
        updateLastUpdatedTable();
    }

    public void removeGroupFromUser(final String groupKey) {
        // Remote
        usersGroupDB.removeGroupFromUser(groupKey);

        // Local
        table.delete(DatabaseHelper.getInstance().getWritableDatabase(), userKey, groupKey);
        updateLastUpdatedTable();
    }

    /**
     * -----------------------
     *      Access to DB
     * -----------------------
     */
    public void observeUserGroupsAddition(final ObjectReceivedHandler<Group> handler) {
        // Get the last-update time from the local db
        Long localUpdateTime = LastUpdatedModel.getInstance().getLastUpdateTime(table.getTableName());

        if (localUpdateTime != null && localUpdateTime != 0L) {
            // -----------------------------
            // Handler for query observation
            // -----------------------------

            // Receives a List of Group-keys
            ObjectReceivedHandler<List<String>> queryHandler = new ObjectReceivedHandler<List<String>>() {
                @Override
                public void onObjectReceived(List<String> groupKeys) {
                    // TODO: If we are up to date, nothing comes back from Remote so this method isn't called..
                    // Reset the array of groups. We got a new array.
                    groups.clear();

                    // We got the new Groups from remote.
                    // Get the old Groups from local.
                    List<String> groupKeysFromLocal = getGroupsFromLocal();

                    // Merge the keys we received from local & remote.
                    // NOTE: In case there are duplicates, it will be checked when handling each group individually.
                    //       (We add the remote first, so the local-duplicates will be discarded)
                    groupKeys.addAll(groupKeysFromLocal);

                    // Handle the groupKeys we received
                    handleUserGroups(groupKeys, handler);
                }
            };
            // Observe only if the remote update-time is after the the local
            usersGroupDB.getUserGroupsByLastUpdateDate(localUpdateTime, queryHandler);
        }
        else {
            // -------------------------------
            // Handler for regular observation
            // -------------------------------
            ObjectReceivedHandler<List<String>> observationHandler = new ObjectReceivedHandler<List<String>>() {
                @Override
                public void onObjectReceived(List<String> groupKeys) {
                    // Reset the array of groups. We got a new array.
                    groups.clear();

                    // Handle the groupKeys we received
                    handleUserGroups(groupKeys, handler);
                }
            };

            // Observe all group records from remote group node
            usersGroupDB.getAllUserGroups(observationHandler);
        }
    }

    public void observeUserGroupsDeletion(final ObjectReceivedHandler<Group> handler) {
        // ----------------------------
        //  Handler for Group deletion
        // ----------------------------

        // Receives the removed Group-Key.
        ObjectReceivedHandler<String> groupDeletedHanlder = new ObjectReceivedHandler<String>() {
            @Override
            public void onObjectReceived(String groupKey) {
                Integer groupIndex = getGroupIndexByKey(groupKey);

                if (groupIndex != null) {
                    // Remove the group from memory
                    Group removedGroup = groups.remove(groupIndex.intValue());

                    handler.onObjectReceived(removedGroup);
                }
            }
        };

        // Observe all removed groups from remote group node.
        usersGroupDB.observeUserGroupsDeletion(groupDeletedHanlder);
    }

    private List<String> getGroupsFromLocal() {
        // Get the group keys from local db
        return table.getUserGroupKeys(DatabaseHelper.getInstance().getWritableDatabase(), userKey);
    }

    private void handleUserGroupAddition(String groupKey, final ObjectReceivedHandler<Group> handler) {
        ObjectReceivedHandler<Group> receivedGroupHandler = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group group) {
                // If the group doesn't already exist (Just in case..)
                if (!containsGroup(group)) {
                    groups.add(group);
                    Collections.sort(groups);
                }

                handler.onObjectReceived(group);
            }
        };

        // Retrieve the Group object
        GroupsModel.getInstance().findGroupByKey(groupKey, receivedGroupHandler);
    }

    /**
     * -----------------------
     *     Side-functions
     * -----------------------
     */

    /**
     * This function is only for lists received from remote db
     */
    private void handleUserGroups(List<String> groupKeys, ObjectReceivedHandler<Group> handler) {
        // Handle each received group key individually
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }

        // After adding all the groups and filtering duplicates, get the new final list of group keys.
        final List<String> filteredGroupKeys = new ArrayList<>();

        synchronized (groups) {
            for (Group g : groups) {
                filteredGroupKeys.add(g.getKey());
            }
        }

        // Update local records.
        table.truncate(DatabaseHelper.getInstance().getWritableDatabase());
        table.insertGroupKeys(DatabaseHelper.getInstance().getWritableDatabase(), userKey, filteredGroupKeys);

        updateLastUpdatedTable();
    }

    private void updateLastUpdatedTable() {
        updateLastUpdatedTable(table.getTableName());
    }

    private Integer getGroupIndexByKey(String groupKey) {
        synchronized (groups) {
            for (Group group : groups) {
                if (group.getKey().equals(groupKey)) {
                    return groups.indexOf(group);
                }
            }
        }

        return null;
    }

    /**
     * ----------------------
     *  Container Functions
     * ----------------------
     */

    public int getGroupsCount() {
        return groups.size();
    }

    public Group getGroup(int position) {
        if (position < getGroupsCount()) {
            return groups.get(position);
        }

        return null;
    }

    public List<Group> getAllGroups() {
        // Create a read-only copy of the list of groups.
        return Collections.unmodifiableList(groups);
    }

    private Boolean containsGroup(Group group) {
        synchronized (groups) {
            for (Group g : groups) {
                if (g.getKey().equals(group.getKey())) {
                    return true;
                }
            }
        }

        return false;
    }
}
