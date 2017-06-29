package com.example.admin.myapplication.controller.database.remote;

import android.util.Log;

import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;
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
public class UsersDB {
    private static final String TAG = "UsersDB";
    private static final String USERS_NODE_URL = "users";
    private DatabaseReference usersRef;
    private Map<String, User> userCache = new HashMap<>();

    public UsersDB() {
        usersRef = FirebaseDatabase.getInstance().getReference().child(USERS_NODE_URL);
    }

    public void findUserByKey(final String userKey, final ObjectReceivedHandler<User> handler) {
        if (userCache.get(userKey) == null) {
            // Read from the database
            usersRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = mapToUser(userKey, (Map<String, Object>) dataSnapshot.getValue());
                        userCache.put(userKey, user);
                        handler.onObjectReceived(user);
                    }
                    else {
                        handler.onObjectReceived(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read user for key: " + userKey, error.toException());
                }
            });
        }
        else {
            handler.onObjectReceived(userCache.get(userKey));
        }
    }

    public void findUserByFacebookId(final String facebookId, final ObjectReceivedHandler<User> handler) {
        usersRef.orderByChild(User.FACEBOOK_ID_STRING).equalTo(facebookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Go over all the users that we received (Supposedly only 1 user).
                    for (Map.Entry<String, Map<String, Object>> entry : ((Map<String, Map<String, Object>>)dataSnapshot.getValue()).entrySet()) {
                        String userKey = entry.getKey();
                        Map<String, Object> userValue = entry.getValue();

                        // Extract the User object
                        User user = mapToUser(userKey, userValue);
                        handler.onObjectReceived(user);

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read user for facebookId: " + facebookId, error.toException());
            }
        });
    }

    public void addNewUser(User user) {
        // Don't need to generate a new key, because the key was generated by Auth service.
        String userKey = user.getKey();
        Map<String, Object> postValues = user.toMap();

        // Set the values
        usersRef.child(userKey).setValue(postValues);

        userCache.put(userKey, user);
    }

    private User mapToUser(String userKey, Map<String, Object> values) {
        String userName = (String) values.get(User.NAME_STRING);
        String facebookId = (String) values.get(User.FACEBOOK_ID_STRING);

        return new User(userKey, facebookId, userName);
    }
}