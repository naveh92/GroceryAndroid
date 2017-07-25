package com.android_project.grocery.controller.database.remote;

import android.util.Log;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;
import com.android_project.grocery.controller.database.models.ListsModel;
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
 *
 * NOTE: We cannot create a query to fetch by updateTime, because Firebase doesn't allow 2 filters.
 *       Therefore, there is no "observeListsByLastUpdateTime()" function.
 */
public class GroceryListsByGroupDB {
    private static final String LISTS_NODE_URL = "grocery-lists";
    private static final String TAG = "GroceryListsByGroupDB";
    private Query query;
    private ArrayList<ChildEventListener> queryChildListenList = new ArrayList<>();

    public GroceryListsByGroupDB(String groupKey) {
        query = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL).orderByChild(GroceryList.GROUP_KEY_STRING).equalTo(groupKey);
    }

    /**
     * Observe all lists for this group.
     */
    public void observeLists(final ObjectReceivedHandler<GroceryList> listAddedHandler,
                             final ObjectReceivedHandler<GroceryList> listRemovedHandler) {


        ChildEventListener queryChildListen = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                receivedChildAdded(dataSnapshot, listAddedHandler);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                receivedChildChanged(dataSnapshot, listRemovedHandler);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to retrieve Grocery-lists..");
            }
        });
        queryChildListenList.add(queryChildListen);
    }

    public static void deleteAllListsForGroup(final String deletedGroupKey) {
        Query q = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL).orderByChild(GroceryList.GROUP_KEY_STRING).equalTo(deletedGroupKey);

        // Query all lists for this group
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> listsKeys = new ArrayList<>();

                // Add the keys of all the lists for this group.
                for (DataSnapshot listValue : dataSnapshot.getChildren()) {
                    listsKeys.add(listValue.getKey());
                }

                // Delete every list from DB
                for (String listKey : listsKeys) {
                    ListsModel.getInstance().deleteList(listKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to retrieve Grocery-lists..");
            }
        });
    }

    /**
     * ----------------
     *  Util functions
     * ----------------
     */
    private void receivedChildAdded(DataSnapshot dataSnapshot, ObjectReceivedHandler<GroceryList> listAddedHandler) {
        GroceryList addedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());
        listAddedHandler.onObjectReceived(addedList);
    }

    private void receivedChildChanged(DataSnapshot dataSnapshot, ObjectReceivedHandler<GroceryList> listRemovedHandler) {
        GroceryList removedList = mapToGroceryList(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());

        // Check if the list was deleted (archived)
        if (!removedList.isRelevant()) {
            listRemovedHandler.onObjectReceived(removedList);
        }
    }

    private GroceryList mapToGroceryList(String key, Map<String, Object> values) {
        String groupKey = (String) values.get(GroceryList.GROUP_KEY_STRING);
        String title = (String) values.get(GroceryList.TITLE_STRING);
        Boolean relevant = (Boolean) values.get(GroceryList.RELEVANT_STRING);

        return new GroceryList(key, groupKey, title , relevant);
    }

    public void Destroy(){
        if (!queryChildListenList.isEmpty()){
            for (ChildEventListener item:
                    queryChildListenList) {
                query.removeEventListener(item);

            }
        }
    }

}