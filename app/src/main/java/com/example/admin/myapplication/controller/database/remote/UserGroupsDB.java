package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

    public UserGroupsDB(String userKey) {
        this.userKey = userKey;
        userRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + "/" + userKey);
        userGroupsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + "/" + userKey + "/" + GROUPS_NODE_URL);
    }

    public void observerUserGroupsAddition(final ObjectReceivedHandler handler) {
        // TODO:
        // Get the last-update time in the local db
        Long localUpdateTime = null; // LastUpdateTable.getLastUpdateDate();

        if (localUpdateTime != null) {
            // -----------------------------
            // Handler for query observation
            // -----------------------------
            // Observe only if the remote update-time is after the the local
            // TODO: localUpdateTime.toFirebase()?
            userRef.orderByChild("lastUpdated").startAt(localUpdateTime).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Reset the list of groups. We've got a new array.
                    groups.clear();

                    // If we got groups
                    if (dataSnapshot.exists()) {
                        Map<String, Object> groupNodeValue = (Map<String, Object>) ((Map<String, Object>) dataSnapshot.getValue()).values();

                        // Create a list containing the group keys.
                        List<String> groupKeys = new ArrayList<>();
                        groupKeys.addAll(groupNodeValue.keySet());

                        // The list contains the lastUpdated value
                        if (groupKeys.contains("lastUpdated")) {
                            // Remove it
                            groupKeys.remove("lastUpdated");
                        }

                        // Handle the groupKeys we received
                        handleUserGroups(groupKeys, handler);
                    }
                    else {
                        // Local DB is up to date - get groups from local.
                        getGroupsFromLocal(handler);
                    }
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
                    // Reset the array of groups. We've got a new array.
                    groups.clear();

                    // If we got groups
                    if (dataSnapshot.exists()) {
                        // Create a list containing the received groupKeys
                        List<String> groupKeys = new ArrayList<>();
                        groupKeys.addAll(((Map<String, Object>)dataSnapshot.getValue()).keySet());

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

    private void getGroupsFromLocal(ObjectReceivedHandler handler) {
        // TODO:
//        let groupKeysFromLocal = UserGroupsTable.getUserGroupKeys(database: LocalDb.sharedInstance?.database)
//
//        for groupKey in groupKeysFromLocal {
//            self.handleUserGroupAddition(groupKey: groupKey, whenGroupAdded: whenGroupAdded)
//        }
    }

    private void handleUserGroups(List<String> groupKeys, ObjectReceivedHandler handler) {
        // Handle each group individually.
        for (String groupKey : groupKeys) {
            handleUserGroupAddition(groupKey, handler);
        }

        // TODO: Update local records.
//        UserGroupsTable.truncateTable(database: LocalDb.sharedInstance?.database)
//        UserGroupsTable.addGroupKeys(database: LocalDb.sharedInstance?.database, groupKeys: groupKeys)
//        LastUpdateTable.setLastUpdate(database: LocalDb.sharedInstance?.database,
//                table: UserGroupsTable.TABLE,
//                key: self.userKey as String,
//                lastUpdate: Date())
    }

    private void handleUserGroupAddition(String groupKey, final ObjectReceivedHandler handler) {
        ObjectReceivedHandler receivedGroupHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                Group group = (Group) obj;
                groups.add(group);

                // Checking index explicitly - For multithreading safety
                int newGroupIndex = groups.indexOf(group);
                handler.onObjectReceived(newGroupIndex);
            }

            @Override
            public void removeAllObjects() {}
        };

        // Retrieve the Group object
        GroupsDB.getInstance().findGroupByKey(groupKey, receivedGroupHandler);
    }

    public void observeUserGroupsDeletion(final ObjectReceivedHandler handler) {
        userGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int groupIndex = getGroupIndexByKey(dataSnapshot.getKey());

                // Remove the group from memory
                Group removedGroup = groups.remove(groupIndex);

                handler.onObjectReceived(groupIndex);

                // TODO: Send the group as well?
//                whenGroupDeleted(groupIndex, removedGroup)
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

    public void addGroupToUser(String groupKey) {
        // TODO: new Date().toFirebase()?
        Map<String, Object> valuesToPost = new HashMap<>();
        valuesToPost.put(groupKey, true);
//        valuesToPost.put("lastUpdated", )

        userGroupsRef.updateChildren(valuesToPost);
    }

    public void removeGroupFromUser(String groupKey) {
        userGroupsRef.child(groupKey).removeValue();

        // TODO: new Date().toFirebase()?
        Map<String, Object> valuesToPost = new HashMap<>();
//        valuesToPost.put("lastUpdated", )
//        userGroupsRef.updateChildren(valuesToPost);
    }

    // TODO: ?
//    func removeObservers() {
//        userRef.removeAllObservers()
//        userGroupsRef.removeAllObservers()
//    }

    public int getGroupsCount() {
        return groups.size();
    }


    public Group getGroup(int position) {
        if (position < getGroupsCount()) {
            return groups.get(position);
        }

        return null;
    }
}