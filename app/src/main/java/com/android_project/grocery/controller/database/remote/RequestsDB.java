package com.android_project.grocery.controller.database.remote;

import android.util.Log;

import com.android_project.grocery.model.entities.GroceryRequest;
import com.android_project.grocery.controller.handlers.ObjectHandler;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ArrayList<ValueEventListener> addRequestsListenerList = new ArrayList<>();;

    public RequestsDB(String listKey) {
        requestsRef = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL + DELIMITER + listKey + DELIMITER + REQUESTS_NODE_URL);
        TAG += " (" + listKey + ")";
    }

    // --------------
    //    Requests
    // --------------
    /**
     * This function executes a query in the remote DB, searching for records that were updated AFTER lastUpdated parameter.
     */
    public void observeRequestsByLastUpdateDate(Long localUpdateTime, final ObjectHandler<GroceryRequest> handler) {
        // Observe only if the remote update-time is after the the local
        requestsRef.orderByChild(LAST_UPDATED_STRING).startAt(localUpdateTime).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handleDataSnapshot(dataSnapshot, handler);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read grocery-lists value.", error.toException());
            }
        });
    }

    public void observeAllRequests(final ObjectHandler<GroceryRequest> handler) {
        // Read from the database
        ValueEventListener addRequestsListener = requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // No need to remove all requests and then add them back -
                // We perform a merge between the old & new data in the Adapter tier.
                handleDataSnapshot(dataSnapshot, handler);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read grocery-lists value.", error.toException());
            }
        });
        addRequestsListenerList.add(addRequestsListener);
    }

    private void handleDataSnapshot(DataSnapshot incomingSnapshot, ObjectHandler<GroceryRequest> handler) {
        for (DataSnapshot child : incomingSnapshot.getChildren()) {
            // Extract the object from the map
            Map<String, Object> values = ((Map<String, Object>)child.getValue());
            GroceryRequest request = mapToRequest(child.getKey(), values);

            handler.onObjectReceived(request);
        }
    }

    private GroceryRequest mapToRequest(String requestKey, Map<String, Object> values) {
        String userKey = (String) values.get(GroceryRequest.USER_ID_STRING);
        String itemName = (String) values.get(GroceryRequest.ITEM_NAME_STRING);
        Boolean purchased = Boolean.valueOf((String)values.get(GroceryRequest.PURCHASED_STRING));
        Long updateTime = (Long) values.get(LAST_UPDATED_STRING);

        return new GroceryRequest(requestKey, userKey, itemName, purchased);
    }

    public void addNewRequest(final GroceryRequest request, final ObjectReceivedHandler<String> generatedKeyHandler) {
        ObjectReceivedHandler<Long> timestampHandler = new ObjectReceivedHandler<Long>() {
            @Override
            public void onObjectReceived(Long currentRemoteDate) {
                // Generate a key for the new request
                String key = requestsRef.push().getKey();
                Map<String, Object> postValues = request.toMap();

                postValues.put(LAST_UPDATED_STRING, currentRemoteDate);

                // Set the values
                requestsRef.child(key).setValue(postValues);

                generatedKeyHandler.onObjectReceived(key);

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

    public void Destroy(){
        if (!addRequestsListenerList.isEmpty()){
            for (ValueEventListener item:
                addRequestsListenerList) {

                requestsRef.removeEventListener(item);
            }
        }
    }
}
