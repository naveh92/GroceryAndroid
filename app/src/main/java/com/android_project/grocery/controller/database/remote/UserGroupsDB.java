package com.android_project.grocery.controller.database.remote;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
public class UserGroupsDB extends AbstractRemoteDB {
    private static final String USERS_NODE_URL = "users";
    private static final String GROUPS_NODE_URL = "groups";
    private static final String LAST_UPDATED_STRING = "lastUpdated";
    private static final String DELIMITER = "/";
    private DatabaseReference userRef;
    private DatabaseReference userGroupsRef;

    /**
     * The key is the Query reference, the value is the List of ValueEventListeners for that query
     */
    private Map<Query, List<ValueEventListener>> userRefListeners = new HashMap<>();
    private ArrayList<ValueEventListener> userGroupsRefListeners = new ArrayList<>();

    public UserGroupsDB(String userKey) {
        userRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + DELIMITER + userKey);
        userGroupsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE_URL + DELIMITER + userKey + DELIMITER + GROUPS_NODE_URL);
    }

    /**
     * This function executes a query in the remote DB, searching for records that were updated AFTER lastUpdated parameter.
     */
    public void getUserGroupsByLastUpdateDate(Long lastUpdated, final ObjectReceivedHandler<Map<String, Boolean>> handler) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If we got groups
                if (dataSnapshot.exists()) {
                    Map<String, Object> groupNodeValue = (Map<String, Object>) ((Map<String, Object>) dataSnapshot.getValue()).get(GROUPS_NODE_URL);

                    // Extract the entries (GroupKey, Relevant) from the Map.
                    Map<String, Boolean> groupEntries = extractEntries(groupNodeValue);
                    handler.onObjectReceived(groupEntries);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        // Observe only if the remote update-time is after the the local
        Query query = userRef.orderByChild(LAST_UPDATED_STRING).startAt(lastUpdated);
        query.addValueEventListener(listener);
        addListenerToQuery(query, listener);
    }

    private void addListenerToQuery(Query query, ValueEventListener listener) {
        // If the map doesn't contain a list for this reference, create it.
        if (!userRefListeners.containsKey(query)) {
            userRefListeners.put(query, new ArrayList<ValueEventListener>());
        }

        // Add the listener to this references list
        userRefListeners.get(query).add(listener);
    }

    /**
     * This function observes (does NOT query) the remote DB for all Groups.
     */
    public void getAllUserGroups(final ObjectReceivedHandler<List<String>> handler) {
        ValueEventListener listener = new ValueEventListener() {
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
            public void onCancelled(DatabaseError databaseError) {}
        };

        userGroupsRef.addValueEventListener(listener);
        userGroupsRefListeners.add(listener);
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
    private Map<String, Boolean> extractEntries(Map<String, Object> groupsNodeValues) {
        // Create a list containing the received (relevant) groupKeys
        Map<String, Boolean> groupKeys = new HashMap<>();

        for (String key : groupsNodeValues.keySet()) {
            if (!UserGroupsDB.LAST_UPDATED_STRING.equals(key)) {
                Boolean relevant = (Boolean) groupsNodeValues.get(key);
                groupKeys.put(key, relevant);
            }
        }

        return groupKeys;
    }
    private List<String> getRelevantGroupKeys(Map<String, Object> groupsNodeValues) {
        // Create a list containing the received (relevant) groupKeys
        List<String> groupKeys = new ArrayList<>();

        // Managing the relevant groups from the current user groups
        for (Map.Entry<String, Object> entry : groupsNodeValues.entrySet()) {
            String key = entry.getKey();

            // Add this group only if it's relevant.
            if (key != null && !key.equals(UserGroupsDB.LAST_UPDATED_STRING) && (Boolean) entry.getValue()) {
                groupKeys.add(key);
            }
        }

        // The list contains the lastUpdated value
        if (groupKeys.contains(UserGroupsDB.LAST_UPDATED_STRING)) {
            // Remove it
            groupKeys.remove(UserGroupsDB.LAST_UPDATED_STRING);
        }

        return groupKeys;
    }

    @Override
    public void removeListeners() {
        // Go over all entries in the userRef Listener map
        for (Query currentQuery : userRefListeners.keySet()) {
            List<ValueEventListener> currentRefListeners = userRefListeners.get(currentQuery);

            if (currentRefListeners != null) {
                // Go over all the listeners we added to this reference
                for (ValueEventListener currentListener : currentRefListeners) {
                    currentQuery.removeEventListener(currentListener);
                }
            }
        }

        // Remove all listeners from userGroups ref
        for (ValueEventListener listener : userGroupsRefListeners) {
            userGroupsRef.removeEventListener(listener);
        }
    }
}