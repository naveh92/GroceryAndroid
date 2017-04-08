package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ListReceivedHandler;
import com.example.admin.myapplication.controller.handlers.RequestReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.GroceryRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class ListsDB {
    private static String TAG = "ListsDB";
    private static ListsDB instance;

    private DatabaseReference listsRef; private static final String LISTS_NODE_URL = "grocery-lists";

    private ListsDB() {
        listsRef = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL);
    }

    public static ListsDB getInstance() {
        if (instance == null) {
            instance = new ListsDB();
        }

        return instance;
    }

    // TODO:
//    public void observeListsDeletion(final ListReceivedHandler handler) {
//        // Read from the database
//        listsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Map<String, Object> values = ((Map<String, Object>)dataSnapshot.getValue());
//                GroceryList removedList = mapToGroceryList(dataSnapshot.getKey(), values);
//                handler.onListReceived(removedList);
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//
//            private GroceryList mapToGroceryList(String key, Map<String, Object> values) {
//                String groupKey = (String) values.get("groupKey");
//                String title = (String) values.get("title");
//
//                return new GroceryList(key, groupKey, title);
//            }
//        });
//    }

    public void addNewList(GroceryList list) {
        // Generate a key for the new list
        String key = listsRef.push().getKey();
        Map<String, Object> postValues = list.toMap();

        // TODO: lastUpdated

        // Set the values
        listsRef.child(key).setValue(postValues);
    }

    public void deleteList(String listKey) {
        listsRef.child(listKey).removeValue();

        // TODO: Update in localDB as well
    }
}
