package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.UserGroupsTable;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private List<String> groupKeysFromLocal = new ArrayList<>();

    public UserGroupsModel(String userKey) {
        this.userKey = userKey;
        this.usersGroupDB = new UserGroupsDB(userKey);

        // Make sure the local db is initialized
        if (table == null) {
            table = new UserGroupsTable();
        }
    }

    public void addGroupToUser(String groupKey) {
        // Local
        table.insert(userKey, groupKey);
        updateLastUpdatedTable();

        // Remote
        usersGroupDB.addGroupToUser(groupKey);
    }

    public void removeGroupFromUser(final String groupKey) {
        // Local
        table.delete(userKey, groupKey);
        updateLastUpdatedTable();

        // Remote
        usersGroupDB.removeGroupFromUser(groupKey);
    }

    /**
     * -----------------------
     *      Access to DB
     * -----------------------
     */
    public void observeUserGroupsAddition(final ObjectReceivedHandler<Group> handler) {
        // Get the last-update time from the local db
        Long localUpdateTime = LastUpdatedModel.getInstance().getLastUpdateTime(table.getTableName(), userKey);

        if (localUpdateTime != null && localUpdateTime != 0L) {
            // -----------------------------
            // Handler for query observation
            // -----------------------------

            // Receives a List of Group-keys
            ObjectReceivedHandler<Map<String, Boolean>> queryHandler = new ObjectReceivedHandler<Map<String,Boolean>>() {
                @Override
                public void onObjectReceived(Map<String, Boolean> groupEntries) {
                    // NOTE: If we are up to date, nothing comes back from Remote so this method isn't called..

                    // Handle the groupKeys we received
                    handleUserGroups(groupEntries, handler);
                }
            };

            // Reset the array of groups. We got a new array.
            groups.clear();

            // Retrieve from local DB before remote DB.
            // Get the old Groups from local.
            List<String> groupKeysFromLocal = getGroupsFromLocal();

            // Merge the keys we received from local & remote.
            // NOTE: In case there are duplicates, it will be checked when handling each group individually.
            //       (We add the remote first, so the local-duplicates will be discarded)

            // Handle the groupKeys we received from local
            handleUserGroupsFromLocal(groupKeysFromLocal, handler);

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
                    handleUserGroupsFromRemote(groupKeys, handler);
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
        return table.getUserGroupKeys(userKey);
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
     * This function is only for lists received from remote db, AFTER the lists have arrived from the local db.
     * The function merges the data from remote & local.
     */
    private void handleUserGroups(Map<String, Boolean> groupEntries, ObjectReceivedHandler<Group> handler) {
        // Merge the local & remote into a clean list
        groups.clear();

        // Make sure all the groups in local are still relevant
        // Explanation:
        //     - If group was deleted by us, it shouldn't be in localDB - all good.
        //     - If group was deleted by someone else, it will be in groupEntries, but it will not be relevant - filter it out later.
        //     - If group was not deleted, and local is newer, the group will not show up in the remote query - so add it.
        //     - If group was not deleted, and remote is newer, the group will show up in the remote, so we don't want to overwrite it.
        for (String groupKey : groupKeysFromLocal) {
            if (!groupEntries.containsKey(groupKey)) {
                // Remote doesn't have this key, which means we should use the local data.
                groupEntries.put(groupKey, true);
            }
        }

        // Create a list in which only the relevant group keys will be.
        final List<String> relevantGroupKeys = new ArrayList<>();

        // Handle each received group key individually
        for (String groupKey : groupEntries.keySet()) {
            Boolean relevant = groupEntries.get(groupKey);

            // Use only the relevant (Non-deleted) records.
            if (relevant != null && relevant) {
                relevantGroupKeys.add(groupKey);
                handleUserGroupAddition(groupKey, handler);
            }
        }

        // Update local records.
        table.truncate();
        table.insertGroupKeys(userKey, relevantGroupKeys);

        updateLastUpdatedTable();
    }

    /**
     * This function is only for lists received from remote db, when we ONLY QUERY from remote DB.
     */
    private void handleUserGroupsFromRemote(List<String> groupKeys, ObjectReceivedHandler<Group> handler) {
        // Handle each received group key individually
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }

        // Update local records.
        table.truncate();
        table.insertGroupKeys(userKey, groupKeys);

        updateLastUpdatedTable();
    }

    /**
     * This function is only for lists received from local db
     */
    private void handleUserGroupsFromLocal(List<String> groupKeys, ObjectReceivedHandler<Group> handler) {
        groupKeysFromLocal = groupKeys;

        // Handle each received group key individually
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }
    }

    private void updateLastUpdatedTable() {
        updateLastUpdatedTable(table.getTableName(), userKey);
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