package com.android_project.grocery.controller.database.remote;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class ListsDB {
    private static final String LAST_UPDATED_STRING = "lastUpdated";
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

    public void addNewList(final GroceryList list, final ObjectReceivedHandler<String> generatedKeyHandler) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                // Generate a key for the new list
                String key = listsRef.push().getKey();
                Map<String, Object> postValues = list.toMap();
                postValues.put(LAST_UPDATED_STRING, currentRemoteDate);

                // Set the values
                listsRef.child(key).setValue(postValues);

                generatedKeyHandler.onObjectReceived(key);
            }
        };

        // Fetch the timestamp from the firebase server.
        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    public void deleteList(String listKey) {
        Map<String, Object> relevance = new HashMap<String, Object>();
        relevance.put(GroceryList.RELEVANT_STRING , false);
        listsRef.child(listKey).updateChildren(relevance);
    }
}
