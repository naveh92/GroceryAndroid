package com.example.admin.myapplication.controller.database.remote;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by admin on 07/04/2017.
 */
public class AuthenticationManager {
    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
