package com.android_project.grocery.controller.database.remote;

import android.util.Log;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 05/04/2017.
 */
public class GroupsDB {
    private static final String TAG = "GroupsDB";
    private static GroupsDB instance;
    private static final String GROUPS_NODE_URL = "groups";
    private DatabaseReference groupsRef;

    private GroupsDB() {
        groupsRef = FirebaseDatabase.getInstance().getReference().child(GROUPS_NODE_URL);
    }
    public static GroupsDB getInstance() {
        if (instance == null) {
            instance = new GroupsDB();
        }

        return instance;
    }

    public String addNewGroup(Group group) {
        // Generate a key for the new group
        String generatedKey = groupsRef.push().getKey();
        Map<String, Object> postValues = group.toMap();

        // Set the values
        groupsRef.child(generatedKey).setValue(postValues);

        return generatedKey;
    }

    public void deleteGroup(String key) {
        Map<String, Object> relevance = new HashMap<>();
        relevance.put(Group.RELEVANT_STRING , false);
        groupsRef.child(key).updateChildren(relevance);
    }

    public void findGroupByKey(String key, final ObjectReceivedHandler<Group> handler) {
        // Read from the database
        groupsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Extract the object
                Group group = mapToGroup(dataSnapshot.getKey(), (Map<String, Object>)dataSnapshot.getValue());
                handler.onObjectReceived(group);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group value.", error.toException());
            }
        });
    }

    private Group mapToGroup(String groupKey, Map<String, Object> values) {
        String title = (String) values.get(Group.TITLE_STRING);
        Boolean relevant = (Boolean) values.get(Group.RELEVANT_STRING);

        return new Group(groupKey, title, relevant);
    }
}
