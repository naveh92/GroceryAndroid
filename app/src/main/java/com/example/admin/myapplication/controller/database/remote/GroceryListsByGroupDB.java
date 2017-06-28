package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
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

    public void observeLists(final ObjectReceivedHandler<GroceryList> listAddedHandler,
                             final ObjectReceivedHandler<GroceryList> listRemovedHandler) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroceryList addedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());
                listAddedHandler.onObjectReceived(addedList);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroceryList removedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());

                if (removedList.getIsArchived())
                    listRemovedHandler.onObjectReceived(removedList);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

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
                Boolean archive = (Boolean) values.get("archive");

                if (archive == null) {
                    archive = false;
                }

                return new GroceryList(key, groupKey, title , archive);
            }
        });
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void deleteAllListsForGroup() {
        // Query all lists for this group
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> listsKeys = new ArrayList<>();

                // Add the keys of all the lists for this group.
                for (DataSnapshot listValue : dataSnapshot.getChildren()) {
                    listsKeys.add(listValue.getKey());
                }

                // Delete every list from DB
                for (String listKey : listsKeys) {
                    ListsDB.getInstance().deleteList(listKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to retrieve Grocery-lists..");
            }
        });
    }

    // TODO: ?
//    func removeObservers() {
//        query.removeAllObservers()
//    }
}