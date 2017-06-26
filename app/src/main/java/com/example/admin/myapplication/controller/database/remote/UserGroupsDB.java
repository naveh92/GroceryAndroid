package com.example.admin.myapplication.controller.database.remote;

import android.content.Context;
import android.util.Log;

import com.example.admin.myapplication.controller.database.local.DatabaseHelper;
import com.example.admin.myapplication.controller.database.local.UserGroupsTable;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 06/04/2017.
 */
public class UserGroupsDB {
    private static final String USERS_NODE_URL = "users";
    private static final String GROUPS_NODE_URL = "groups";
    private DatabaseReference userRef;
    private DatabaseReference userGroupsRef;
    private List<Group> groups = new ArrayList<>();
    private String userKey;
    // TODO: Need to table.close() onDestroy()??
    private UserGroupsTable table;

    public UserGroupsDB(String userKey) {
        this.userKey = userKey;
        userRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + "/" + userKey);
        userGroupsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + "/" + userKey + "/" + GROUPS_NODE_URL);
    }

    public void observeUserGroupsAddition(Context context, final ObjectReceivedHandler<Group> handler) {
        // Make sure the local db is initialized
        if (table == null) {
            table = new UserGroupsTable();
        }

        // TODO:
        // Get the last-update time in the local db
        Long localUpdateTime = null; // stUpdateTable.getLastUpdateDate();

        if (localUpdateTime != null) {
            // -----------------------------
            // Handler for query observation
            // -----------------------------
            // Observe only if the remote update-time is after the the local
            // TODO: localUpdateTime.toFirebase()?
            userRef.orderByChild("lastUpdated").startAt(localUpdateTime).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Reset the array of groups. We got a new array.
                    groups.clear();

                    // If we got groups
                    if (dataSnapshot.exists()) {

                        Map<String, Object> groupNodeValue = (Map<String, Object>) ((Map<String, Object>) dataSnapshot.getValue()).values();

                        // Create a list containing the group keys.
                        List<String> groupKeys = new ArrayList<>();

                        // list of not archived user groups
                        HashMap<String, Object> usersGroup = new HashMap<String, Object>();

                        // Sorting the archived groups from the current user groups
                        for (Map.Entry<String, Object> entry : groupNodeValue.entrySet()) {

                            // if is not archived
                            if ((Boolean) entry.getValue()){
                                usersGroup.entrySet().add(entry);
                            }
                            else{
                                //TODO: Handle archived groups
                            }
                        }

                        groupKeys.addAll(usersGroup.keySet());

                        // The list contains the lastUpdated value
                        if (groupKeys.contains("lastUpdated")) {
                            // Remove it
                            groupKeys.remove("lastUpdated");
                        }

                        // Handle the groupKeys we received
                        handleUserGroups(groupKeys, handler);
                    }

                    // Local DB is up to date - get groups from local.
                    getGroupsFromLocal(handler);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
// TODO:
                }
            });
        }
        else {
            // -------------------------------
            // Handler for regular observation
            // -------------------------------
            // Observe all group records from remote group node
            userGroupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // If we got groups
                    if (dataSnapshot.exists()) {
                        // Reset the array of groups. We got a new array.
                        groups.clear();

                        // Create a list containing the received groupKeys
                        List<String> groupKeys = new ArrayList<>();
                        //groupKeys.addAll(((Map<String, Object>)dataSnapshot.getValue()).keySet());

                        Map<String, Object> groupNodeValue = (Map<String, Object>) dataSnapshot.getValue();

                        // list of not archived user groups
                        HashMap<String, Object> usersGroup = new HashMap<String, Object>();

                        // Sorting the archived groups from the current user groups
                        for (Map.Entry<String, Object> entry : groupNodeValue.entrySet()) {

                            // if is not archived and not
                            if (!entry.getKey().equals("lastUpdated") &&
                                    (Boolean) entry.getValue()){
                                usersGroup.put(entry.getKey(), entry.getValue());
                            }
                            else{
                                //TODO: Handle archived groups
                            }
                        }

                        groupKeys.addAll(usersGroup.keySet());


                        // The list contains the lastUpdated value
                        if (groupKeys.contains("lastUpdated")) {
                            // Remove it
                            groupKeys.remove("lastUpdated");
                        }

                        // Handle the groupKeys we received
                        handleUserGroups(groupKeys, handler);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
// TODO:
                }
            });
        }
    }

    private void getGroupsFromLocal(ObjectReceivedHandler<Group> handler) {
        // Get the group keys from local db
        List<String> groupKeys = table.getUserGroupKeys(DatabaseHelper.getInstance().getWritableDatabase() ,userKey);

        // Handle each received group key individually
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }
    }

    /**
     * This function is only for lists received from remote db
     */
    private void handleUserGroups(List<String> groupKeys, ObjectReceivedHandler<Group> handler) {
        // Handle each received group key individually
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }

        // Update local records.
        table.truncate(DatabaseHelper.getInstance().getWritableDatabase());
        table.insertGroupKeys(DatabaseHelper.getInstance().getWritableDatabase(), userKey, groupKeys);

        // TODO: Update LastUpdateTable
//        LastUpdateTable.setLastUpdate(database: LocalDb.sharedInstance?.database,
//                table: UserGroupsTable.TABLE,
//                key: self.userKey as String,
//                lastUpdate: Date())
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
        GroupsDB.getInstance().findGroupByKey(groupKey, receivedGroupHandler);
    }

    private Boolean containsGroup(Group group) {
        // TODO: Synchronized
        for (Group g : groups) {
            if (g.getKey().equals(group.getKey())) {
                return true;
            }
        }

        return false;
    }

    public void observeUserGroupsDeletion(final ObjectReceivedHandler<Group> handler) {
        userGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Integer groupIndex = getGroupIndexByKey(dataSnapshot.getKey());

                if (groupIndex != null) {
                    // Remove the group from memory
                    Group removedGroup = groups.remove(groupIndex.intValue());

                    handler.onObjectReceived(removedGroup);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private Integer getGroupIndexByKey(String groupKey) {
        for (Group group : groups) {
            if (group.getKey().equals(groupKey)) {
                return groups.indexOf(group);
            }
        }

        return null;
    }

    public void addGroupToUser(final String groupKey) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                Map<String, Object> valuesToPost = new HashMap<>();
                valuesToPost.put(groupKey, true);
                valuesToPost.put("lastUpdated", currentRemoteDate);

                userGroupsRef.updateChildren(valuesToPost);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    public void removeGroupFromUser(final String groupKey) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                userGroupsRef.child(groupKey).setValue(false);

                Map<String, Object> valuesToPost = new HashMap<>();
                valuesToPost.put("lastUpdated", currentRemoteDate);
                userGroupsRef.updateChildren(valuesToPost);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    // TODO: ?
//    func removeObservers() {
//        userRef.removeAllObservers()
//        userGroupsRef.removeAllObservers()
//    }

    // --------------------
    // Container functions
    // --------------------
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
}