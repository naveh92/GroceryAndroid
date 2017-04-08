package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.RequestReceivedHandler;
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

    public RequestsDB(String listKey) {
        requestsRef = FirebaseDatabase.getInstance().getReference(LISTS_NODE_URL + "/" + listKey + "/" + REQUESTS_NODE_URL);
        TAG += " (" + listKey + ")";
    }

    // --------------
    //    Requests
    // --------------
    public void observeRequestsAddition(final RequestReceivedHandler handler) {
        // Read from the database
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                handler.removeAllRequests();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> values = ((Map<String, Object>)child.getValue());

                    String requestKey = child.getKey();
                    String userKey = (String) values.get("userId");
                    String itemName = (String) values.get("itemName");
                    Boolean purchased = Boolean.valueOf((String)values.get("purchased"));

                    handler.onRequestReceived(new GroceryRequest(requestKey, userKey, itemName, purchased));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read grocery-lists value.", error.toException());
            }
        });
    }

    public void addNewRequest(GroceryRequest request) {
        // Generate a key for the new list
        String key = requestsRef.push().getKey();
        Map<String, Object> postValues = request.toMap();
        // TODO: FIRDataetfgnmooer
//        postValues.put("lastUpdated", new Date());

        // Set the values
        requestsRef.child(key).setValue(postValues);
    }

    public void togglePurchased(String requestKey, Boolean currentPurchasedValue) {
        Boolean newValue = !currentPurchasedValue;

        // Generate a key for the new list
        Map<String, Object> postValues = new HashMap<>();
        postValues.put("purchased", newValue.toString());
        // TODO: FIRDataetfgnmooer
//        postValues.put("lastUpdated", new Date());

        // Set the values
        requestsRef.child(requestKey).updateChildren(postValues);
    }
}
