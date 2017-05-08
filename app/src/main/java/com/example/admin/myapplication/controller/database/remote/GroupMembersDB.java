package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 06/04/2017.
 */
public class GroupMembersDB {
    private static final String TAG = "GroupMembersDB";
    private static final String GROUPS_NODE_URL = "groups";
    private static final String MEMBERS_NODE_URL = "members";
    private static final String LAST_UPDATED_NODE = "lastUpdateDate";
    private DatabaseReference databaseRef;
    private DatabaseReference lastUpdatedRef;
    private String groupKey;
    private List<User> members = new ArrayList<>();

    public GroupMembersDB(String groupKey) {
        this.groupKey = groupKey;

        // This database reference will be used to fetch the members
        databaseRef = FirebaseDatabase.getInstance().getReference(GROUPS_NODE_URL).child(groupKey).child(MEMBERS_NODE_URL);

        // This database reference will be used to fetch and observe the last update date
        lastUpdatedRef = FirebaseDatabase.getInstance().getReference(GROUPS_NODE_URL).child(groupKey).child(LAST_UPDATED_NODE);
    }

    public void observeGroupMembers(final ObjectHandler<User> handler) {
        // TODO: Go to db and get last updated.
        // TODO: observeLastUpdated(lastUpdateTimeHandler);
        // TODO: Inside the handler when we get the value:
        long remoteLastUpdateTime = System.currentTimeMillis();

        // Check if we need to update.
        if (isLocalDatabaseUpToDate(remoteLastUpdateTime)) {
                fetchGroupMembersFromLocalDB(handler);
        }
        else {
            fetchGroupMembersFromRemoteDBAndUpdateLocalDB(handler, remoteLastUpdateTime);
        }
    }

    private void fetchGroupMembersFromLocalDB(ObjectReceivedHandler<User> handler) {
        // TODO:
    }

    private void fetchGroupMembersFromRemoteDBAndUpdateLocalDB(final ObjectHandler<User> handler, long remoteLastUpdateTime) {
        // Read from the database
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the list - we are about to get a new value.
                members.clear();
                handler.removeAllObjects();

                List<String> userKeys = new ArrayList<>();

                // Extract group member user keys and append them to our user keys array
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String userKey = child.getKey();

                    userKeys.add(userKey);

                    // Handle each member fetched independently
                    handleGroupMemberAddition(userKey, handler);

                    // TODO: Update local db with the fetched user keys
//                    updateLocalDB(userKeys: userKeys, remoteLastUpdateTime: remoteLastUpdateTime)
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group members value.", error.toException());
            }
        });
    }

    private boolean isLocalDatabaseUpToDate(long remoteLastUpdateDate) {
        return false;
    }

    private void handleGroupMemberAddition(String userKey, final ObjectReceivedHandler<User> handler) {
        ObjectReceivedHandler<User> foundUserHandler = new ObjectReceivedHandler<User>() {
            @Override
            public void onObjectReceived(User user) {
                if (user != null) {
                    members.add(user);
                    handler.onObjectReceived(user);
                }
            }
        };

        // Retrieve the user object
        UsersDB.getInstance().findUserByKey(userKey, foundUserHandler);
    }

    private void findGroupMembersCount(final ObjectReceivedHandler<Integer> handler) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    handler.onObjectReceived(0);
                }
                else {
                    handler.onObjectReceived(((Long)dataSnapshot.getChildrenCount()).intValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group mmebers count value.", error.toException());
            }
        });
    }

    public void addMember(String userKey) {
        Map<String, Object> newValues = new HashMap<>();
        newValues.put(userKey, true);

        databaseRef.updateChildren(newValues);

        // TODO: When finished?
        // We updated the members value, so we should set the last updated time.
        updateLastUpdatedTime();
    }

    public void removeMember(String userKey) {
        databaseRef.child(userKey).removeValue();

        // TODO: When finished?
        // We updated the members value, so we should set the last updated time.
        updateLastUpdatedTime();
        deleteGroupIfEmpty();
    }

    private void updateLastUpdatedTime() {
        lastUpdatedRef.setValue(ServerValue.TIMESTAMP);
    }

    private void deleteGroupIfEmpty() {
        ObjectReceivedHandler<Integer> membersCountHandler = new ObjectReceivedHandler<Integer>() {
            @Override
            public void onObjectReceived(Integer count) {
                if (count == 0) {
                    // Delete the group
                    GroupsDB.getInstance().deleteGroup(GroupMembersDB.this.groupKey);

                    // Delete all lists in that group
                    new GroceryListsByGroupDB(GroupMembersDB.this.groupKey).deleteAllListsForGroup();
                }
            }
        };

        findGroupMembersCount(membersCountHandler);
    }

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