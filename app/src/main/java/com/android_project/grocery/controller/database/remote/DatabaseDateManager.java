package com.android_project.grocery.controller.database.remote;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by admin on 10/04/2017.
 */
public class DatabaseDateManager {
    private static final String TIMESTAMP = "timestamp";
    private static DatabaseReference ref = FirebaseDatabase.getInstance().getReference(TIMESTAMP);

    /**
     * This function retrieves the current timestamp from firebase server.
     */
    public static void getTimestamp(final ObjectReceivedHandler<Long> handler) {
        ref.setValue(ServerValue.TIMESTAMP);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long timestamp = (Long) dataSnapshot.getValue();
                handler.onObjectReceived(timestamp);
            }

            public void onCancelled(DatabaseError databaseError) {
                // Failed to get timestamp.
                // User the local timestamp instead
                handler.onObjectReceived(System.currentTimeMillis());
            }
        });
    }
}
