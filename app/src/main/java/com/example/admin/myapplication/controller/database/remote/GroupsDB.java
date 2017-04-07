package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public void observeGroupsAddition(final ObjectReceivedHandler handler) {
        // Read from the database
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                // Clear the list - we are about to getGroup a new value.
                groups.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Group group = mapToGroup(child.getKey(), ((Map<String, Object>)child.getValue()));

                    groups.add(group);
                    handler.onObjectReceived(group);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read groups value.", error.toException());
            }
        });
    }

    public String addNewGroup(Group group, String userKey) {
        // Generate a key for the new group
        String key = groupsRef.push().getKey();
        Map<String, Object> postValues = group.toMap();

        // Set the values
        groupsRef.child(key).setValue(postValues);

        // TODO:
//        new UserGroupsDB(userKey).addGroupToUser(key);
        // TODO: GroupMembersDB?

        return key;
    }

    public void deleteGroup(String key) {
        groupsRef.child(key).removeValue();
    }

    public void findGroupByKey(String key, final ObjectReceivedHandler handler) {
//        if (self.groupCache[key as NSString] == nil) {
        // Read from the database
        groupsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    // Extract the object
                    Group group = mapToGroup(child.getKey(), (Map<String, Object>)child.getValue());

                    groups.add(group);
                    handler.onObjectReceived(group);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read group value.", error.toException());
            }
        });
    }

    private Group mapToGroup(String key, Map<String, Object> values) {
        String groupKey = key;
        String title = (String) values.get("title");

        return new Group(groupKey, title);
    }

    // --------------------
    // Container functions
    // --------------------
    private static List<Group> groups = new ArrayList<>();

    public int groupsNum() {
        return groups.size();
    }

    public Group getGroup(int position) {
        return groups.get(position);
    }

    public String title(String groupKey) {
        // TODO: Java8? groups.stream().filter(group -> group.getKey().equals(groupKey)).collect(Collectors.toList())???
        for (Group group : groups) {
            if (group.getKey().equals(groupKey)) {
                return group.getTitle();
            }
        }

        // TODO: strings.xml
        return "N/A";
    }

    public List<Group> getAllGroups() {
        // Create a read-only copy of the list of groups.
        return Collections.unmodifiableList(groups);
    }
}
