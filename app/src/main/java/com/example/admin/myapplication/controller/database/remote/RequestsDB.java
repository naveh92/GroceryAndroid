package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryRequest;
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
public class RequestsDB {
    private static String TAG = "RequestsDB";
    private DatabaseReference requestsRef;
    private static final String LISTS_NODE_URL = "grocery-lists";
    private static final String REQUESTS_NODE_URL = "requests";
    private static final String LAST_UPDATED_STRING = "lastUpdated";
    private static final String DELIMITER = "/";

    public RequestsDB(String listKey) {
        requestsRef = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL + DELIMITER + listKey + DELIMITER + REQUESTS_NODE_URL);
        TAG += " (" + listKey + ")";
    }

    // --------------
    //    Requests
    // --------------
    public void observeRequestsAddition(final ObjectHandler<GroceryRequest> handler) {
        // Read from the database
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                handler.removeAllObjects();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    // Extract the object from the map
                    Map<String, Object> values = ((Map<String, Object>)child.getValue());
                    GroceryRequest request = mapToRequest(child.getKey(), values);

                    handler.onObjectReceived(request);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read grocery-lists value.", error.toException());
            }
        });
    }

    private GroceryRequest mapToRequest(String requestKey, Map<String, Object> values) {
        String userKey = (String) values.get(GroceryRequest.USER_ID_STRING);
        String itemName = (String) values.get(GroceryRequest.ITEM_NAME_STRING);
        Boolean purchased = Boolean.valueOf((String)values.get(GroceryRequest.PURCHASED_STRING));
        Long updateTime = (Long) values.get(LAST_UPDATED_STRING);

        return new GroceryRequest(requestKey, userKey, itemName, purchased);
    }

    public void addNewRequest(final GroceryRequest request) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                // Generate a key for the new list
                String key = requestsRef.push().getKey();
                Map<String, Object> postValues = request.toMap();

                postValues.put(LAST_UPDATED_STRING, currentRemoteDate);

                // Set the values
                requestsRef.child(key).setValue(postValues);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    public void togglePurchased(final String requestKey, final Boolean currentPurchasedValue) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                Boolean newValue = !currentPurchasedValue;

                // Set the updated values
                Map<String, Object> postValues = new HashMap<>();
                postValues.put(GroceryRequest.PURCHASED_STRING, newValue.toString());
                postValues.put(LAST_UPDATED_STRING, currentRemoteDate);

                // Set the values
                requestsRef.child(requestKey).updateChildren(postValues);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }

    public void updateItemName(final String requestKey, final String newItemName) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                // Set the updated values
                Map<String, Object> postValues = new HashMap<>();
                postValues.put(GroceryRequest.ITEM_NAME_STRING, newItemName);
                postValues.put(LAST_UPDATED_STRING, currentRemoteDate);

                // Set the values
                requestsRef.child(requestKey).updateChildren(postValues);
            }
        };

        DatabaseDateManager.getTimestamp(timestampHandler);
    }
}
