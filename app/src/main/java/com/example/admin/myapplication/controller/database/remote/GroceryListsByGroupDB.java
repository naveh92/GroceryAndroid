package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;

/**
 * Created by admin on 07/04/2017.
 */
public class GroceryListsByGroupDB {
    private static final String LISTS_NODE_URL = "grocery-lists";
    private static final String TAG = "GroceryListsByGroupDB";
    private Query query;
    private String groupKey;

    public GroceryListsByGroupDB(String groupKey) {
        this.groupKey = groupKey;
        query = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL).orderByChild("groupKey").equalTo(groupKey);
    }

    public void observeLists(final ObjectReceivedHandler listAddedHandler,
                             final ObjectReceivedHandler listRemovedHandler) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroceryList addedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());
                listAddedHandler.onObjectReceived(addedList);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // TODO: Need this?
//                GroceryList modifiedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());
//                listChangedHandler.onObjectReceived(modifiedList);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GroceryList removedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());
                listRemovedHandler.onObjectReceived(removedList);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to retrieve Grocery-lists..");
            }

            private GroceryList mapToGroceryList(String key, Map<String, Object> values) {
                String groupKey = (String) values.get("groupKey");
                String title = (String) values.get("title");

                return new GroceryList(key, groupKey, title);
            }
        });
    }

    public String getGroupKey() {
        return groupKey;
    }

    // TODO: ?
//    func removeObservers() {
//        query.removeAllObservers()
//    }
}