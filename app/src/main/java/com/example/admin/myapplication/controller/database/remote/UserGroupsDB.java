package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
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
 * This class manages the access to the Remote DB, but no further logic.
 * It passes every received/deleted object directly to the Model, where the logic is.
 * (The only logic is extracting Group Keys from a DataSnapshot etc.)
 */
public class UserGroupsDB {
    private static final String USERS_NODE_URL = "users";
    private static final String GROUPS_NODE_URL = "groups";
    public static final String LAST_UPDATED_STRING = "lastUpdated";
    private static final String DELIMITER = "/";
    private DatabaseReference userRef;
    private DatabaseReference userGroupsRef;

    public UserGroupsDB(String userKey) {
        userRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + DELIMITER + userKey);
        userGroupsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + DELIMITER + userKey + DELIMITER + GROUPS_NODE_URL);
    }

    /**
     * This function executes a query in the remote DB, searching for records that were updated AFTER lastUpdated parameter.
     */
    public void getUserGroupsByLastUpdateDate(Long lastUpdated, final ObjectReceivedHandler<List<String>> handler) {
        // Observe only if the remote update-time is after the the local
        // TODO: Dafuq? User in db only has 1 global lastUpdated
        // TODO: localUpdateTime.toFirebase()?
        userRef.orderByChild(LAST_UPDATED_STRING).startAt(lastUpdated).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If we got groups
                if (dataSnapshot.exists()) {
                    Map<String, Object> groupNodeValue = (Map<String, Object>) ((Map<String, Object>) dataSnapshot.getValue()).values();

                    // Create a list containing the relevant group keys.
                    List<String> groupKeys = getRelevantGroupKeys(groupNodeValue);

                    // Send the received list of GroupKeys to the Model.
                    handler.onObjectReceived(groupKeys);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
// TODO:
            }
        });
    }

    /**
     * This function observes (does NOT query) the remote DB for all Groups.
     */
    public void getAllUserGroups(final ObjectReceivedHandler<List<String>> handler) {
        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If we got groups
                if (dataSnapshot.exists()) {
                    // Create a list containing the received (relevant) groupKeys
                    List<String> groupKeys = getRelevantGroupKeys((Map<String, Object>) dataSnapshot.getValue());

                    // Send the received list of GroupKeys to the Model.
                    handler.onObjectReceived(groupKeys);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
// TODO:
            }
        });
    }

    /**
     * This function observes the remote DB for all deleted Groups.
     */
    public void observeUserGroupsDeletion(final ObjectReceivedHandler<String> handler) {
        userGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Extract the group key from the snapshot.
                String removedGroupKey = dataSnapshot.getKey();

                // Send the received key to the Model.
                handler.onObjectReceived(removedGroupKey);
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * This function adds a group to the user's groups
     */
    public void addGroupToUser(final String groupKey) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                Map<String, Object> valuesToPost = new HashMap<>();
                // Set the relevance variable to true
                valuesToPost.put(groupKey, true);
                valuesToPost.put(LAST_UPDATED_STRING, currentRemoteDate);

                userGroupsRef.updateChildren(valuesToPost);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    /**
     * This function removes a group from the user's group
     */
    public void removeGroupFromUser(final String groupKey) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                // Set the relevance variable to false
                userGroupsRef.child(groupKey).setValue(false);

                Map<String, Object> valuesToPost = new HashMap<>();
                valuesToPost.put(LAST_UPDATED_STRING, currentRemoteDate);
                userGroupsRef.updateChildren(valuesToPost);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    /**
     * This function gets a Map<String, Object> (extracted from the DataSnapshot)
     * and returns the List of group keys within it.
     */
    private List<String> getRelevantGroupKeys(Map<String, Object> groupsNodeValues) {
        // Create a list containing the received (relevant) groupKeys
        List<String> groupKeys = new ArrayList<>();

        // Managing the relevant groups from the current user groups
        for (Map.Entry<String, Object> entry : groupsNodeValues.entrySet()) {
            String key = entry.getKey();

            // Check if this group is relevant
            if (key != null && !key.equals(UserGroupsDB.LAST_UPDATED_STRING) && (Boolean) entry.getValue()) {
                groupKeys.add(key);
            }
            else {
                // TODO: Handle archived groups
            }
        }

        // The list contains the lastUpdated value
        if (groupKeys.contains(UserGroupsDB.LAST_UPDATED_STRING)) {
            // Remove it
            groupKeys.remove(UserGroupsDB.LAST_UPDATED_STRING);
        }

        return groupKeys;
    }

    // TODO: ?
//    func removeObservers() {
//        userRef.removeAllObservers()
//        userGroupsRef.removeAllObservers()
//    }
}