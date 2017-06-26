package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.model.entities.GroceryList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
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

    public void addNewList(GroceryList list) {
        // Generate a key for the new list
        String key = listsRef.push().getKey();
        Map<String, Object> postValues = list.toMap();

        // TODO: lastUpdated

        // Set the values
        listsRef.child(key).setValue(postValues);
    }

    public void deleteList(String listKey) {

        Map<String, Object> updateArchive = new HashMap<String, Object>();
        updateArchive.put("Archive" , true);
        listsRef.child(listKey).updateChildren(updateArchive);

        // TODO: Update in localDB as well
    }
}
