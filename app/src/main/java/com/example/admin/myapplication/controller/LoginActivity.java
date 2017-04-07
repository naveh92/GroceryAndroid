package com.example.admin.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.facebook.FacebookSdk;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        initAuthenticationListener();
        initFacebookLoginButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initAuthenticationListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                    updateUI(user);

                    // Once we logged-in, move on to the main activity.
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                    // Finish this activity.
                    LoginActivity.this.finish();
                }
                else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    updateUI(null);
                }
            }
        };
    }

    private void initFacebookLoginButton() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook: onSuccess:" + loginResult);
                AuthenticationManager.getInstance().handleFacebookAccessToken(loginResult.getAccessToken(), LoginActivity.this);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook: onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook: onError", error);
                updateUI(null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        AuthenticationManager.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        AuthenticationManager.getInstance().removeAuthStateListener(mAuthListener);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            ((TextView)findViewById(R.id.greetingTV)).setText("Welcome back, " + user.getDisplayName());
        }
        else {
            ((TextView)findViewById(R.id.greetingTV)).setText("Hello");
        }
    }
}