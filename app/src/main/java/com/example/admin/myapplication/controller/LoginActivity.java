package com.example.admin.myapplication.controller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.models.UserModel;
import com.example.admin.myapplication.controller.database.remote.ImageDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;
import com.facebook.AccessToken;
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
    private static Boolean continued;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        // Reset the continued (In case we exited but didn't shut down the app.)
        // Note: When logging-out, we start this activity, so the variable will be reset.
        continued = false;

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

                    AccessToken token = AccessToken.getCurrentAccessToken();

                    // Set the access token.
                    AuthenticationManager.getInstance().setAccessToken(token);

                    // Prevent this from happening twice as long as the app is running.
                    if (!continued) {
                        continued = true;

                        // Manage user creation
                        manageUserCreation(user.getUid(), token.getUserId(), user.getDisplayName());
                    }
                }
                else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    updateUI(null);
                }
            }
        };
    }

    /**
     * This function gets the user object from UserDB.
     * If it can't find it, it creates a new user.
     */
    private void manageUserCreation(final String userKey, final String facebookId, final String name) {
        ObjectReceivedHandler<User> userReceivedHandler = new ObjectReceivedHandler<User>() {
            @Override
            public void onObjectReceived(User receivedUser) {
                if (receivedUser == null) {
                    User user = new User(userKey, facebookId, name);
                    UserModel.getInstance().addNewUser(user);

                    setUserProfilePic(userKey);
                }

                // Once we logged-in, move on to the main activity.
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Finish this activity.
                LoginActivity.this.finish();
            }

            private void setUserProfilePic(final String userKey) {
                ObjectReceivedHandler<Bitmap> profilePicHandler = new ObjectReceivedHandler<Bitmap>() {
                    @Override
                    public void onObjectReceived(Bitmap userProfilePic) {
                        // If succeeded, save it to storage.
                        if (userProfilePic != null) {
                            Log.d(TAG, "Downloaded user Facebook profile pic. Storing...");
                            ImageDB.getInstance().storeImage(LoginActivity.this, userProfilePic, userKey);
                        }
                    }
                };

                // Get the user's profile pic from Facebook
                new FacebookImageManager().downloadUserProfilePic(facebookId, profilePicHandler);
            }
        };

        UserModel.getInstance().findUserByKey(userKey, userReceivedHandler);
    }

    private void initFacebookLoginButton() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setEnabled(true);
        loginButton.setReadPermissions("email", "public_profile","user_friends");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook: onSuccess:" + loginResult);
                AuthenticationManager.getInstance().handleFacebookAccessToken(loginResult.getAccessToken(), LoginActivity.this);
                hideLogoutButton();
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
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
            ((TextView) findViewById(R.id.greetingTV)).setText("Welcome back, " + user.getDisplayName());
             findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            hideLogoutButton();
        }
        else {
            ((TextView) findViewById(R.id.greetingTV)).setText("Hello");
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

            // Enable the facebook button
            findViewById(R.id.button_facebook_login).setVisibility(View.VISIBLE);
        }
    }

    private void hideLogoutButton() {
        // Disable the facebook button
        findViewById(R.id.button_facebook_login).setVisibility(View.INVISIBLE);
    }
}
