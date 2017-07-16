package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
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

    public GroupMembersDB(String groupKey) {
        // This database reference will be used to fetch the members
        databaseRef = FirebaseDatabase.getInstance().getReference(GROUPS_NODE_URL).child(groupKey).child(MEMBERS_NODE_URL);

        // This database reference will be used to fetch and observe the last update date
        lastUpdatedRef = FirebaseDatabase.getInstance().getReference(GROUPS_NODE_URL).child(groupKey).child(LAST_UPDATED_NODE);
    }

    public void fetchGroupMembers(final ObjectReceivedHandler<List<String>> handler) {
        // Read from the database
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userKeys = new ArrayList<>();

                // Extract group member user keys and append them to our user keys array
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    // If the value was not archived (is relevant).
                    if ((Boolean) child.getValue()) {
                        String userKey = child.getKey();
                        userKeys.add(userKey);
                    }
                }

                // Pass the list of keys to the Model.
                handler.onObjectReceived(userKeys);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group members value.", error.toException());
            }
        });
    }

    public void getLastUpdatedTime(final ObjectReceivedHandler<Long> handler) {
        lastUpdatedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handler.onObjectReceived((Long) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO:
            }
        });
    }

    public void findGroupMembersCount(final ObjectReceivedHandler<Integer> handler) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    handler.onObjectReceived(0);
                }
                else {
                    int count = 0;
                    for (DataSnapshot sp : dataSnapshot.getChildren()) {
                        // Make sure this member is relevant
                        if ((Boolean) sp.getValue()) {
                            count++;
                        }
                    }
                    handler.onObjectReceived(count);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group members count value.", error.toException());
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
        databaseRef.child(userKey).setValue(false);

        // TODO: When finished?
        // We updated the members value, so we should set the last updated time.
        updateLastUpdatedTime();
    }

    private void updateLastUpdatedTime() {
        lastUpdatedRef.setValue(ServerValue.TIMESTAMP);
    }
}