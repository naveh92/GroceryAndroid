package com.example.admin.myapplication.controller.authentication;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.admin.myapplication.R;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by admin on 07/04/2017.
 */
public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static AuthenticationManager instance;
    private FirebaseAuth mAuth;
    private AccessToken facebookAccessToken;

    private AuthenticationManager() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public static AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }

        return instance;
    }

    public void addAuthStateListener(FirebaseAuth.AuthStateListener mAuthListener) {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void removeAuthStateListener(FirebaseAuth.AuthStateListener mAuthListener) {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public String getCurrentUserId() {
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser != null) {
            return currentFirebaseUser.getUid();
        }

        return null;
    }

    public void handleFacebookAccessToken(AccessToken token, final Activity context) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // Store the access token to the memory
        setAccessToken(token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, context.getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        this.facebookAccessToken = null;
    }

    public AccessToken getFacebookAccessToken() {
        return facebookAccessToken;
    }

    public void setAccessToken(AccessToken accessToken) { this.facebookAccessToken = accessToken; }

    public void clearAccessToken() { facebookAccessToken = null; }
}
