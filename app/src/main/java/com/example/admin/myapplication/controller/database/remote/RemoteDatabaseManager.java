package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class RemoteDatabaseManager {
    private static String TAG = "RemoteDatabaseManager";
    private static RemoteDatabaseManager instance;
    private FirebaseDatabase database;

    // TODO: Every ref should have its own DB manager. (ListsDB, GroupsDB, etc..)
    private DatabaseReference listsRef; private static final String LISTS_NODE_URL = "grocery-lists";
    private DatabaseReference groupsRef; private static final String GROUPS_NODE_URL = "groups";

    private RemoteDatabaseManager() {
        database = FirebaseDatabase.getInstance();

        listsRef = database.getReference(LISTS_NODE_URL);
        groupsRef = database.getReference(GROUPS_NODE_URL);
    }

    public static RemoteDatabaseManager getInstance() {
        if (instance == null) {
            instance = new RemoteDatabaseManager();
        }

        return instance;
    }

    // --------------
    //     Lists
    // --------------
    public void observeListsAddition(final ObjectReceivedHandler handler) {
        // Read from the database
        listsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                handler.removeAllObjects();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> values = ((Map<String, Object>)child.getValue());

                    String listKey = child.getKey();
                    String groupKey = (String) values.get("groupKey");
                    String title = (String) values.get("title");

                    handler.onObjectReceived(new GroceryList(listKey, groupKey, title));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read grocery-lists value.", error.toException());
            }
        });
    }

    public void addNewList(GroceryList list) {
        // Generate a key for the new list
        String key = listsRef.push().getKey();
        Map<String, Object> postValues = list.toMap();

        // Set the values
        listsRef.child(key).setValue(postValues);
    }
}
