package com.example.admin.myapplication.controller.authentication;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

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
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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

                        // TODO: Try to get the user. if it doesn't exist, create a new user.

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
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

//    private class AccessTokenManager {
//        // TODO: Strings.xml
//        private static final String fileName = "accessToken";
//        private Context context;
//
//        public AccessTokenManager(Context context) {
//            this.context = context;
//        }
//
//        public void storeAccessToken(AccessToken token) {
//            FileOutputStream fos = null;
//            ObjectOutputStream os = null;
//            try {
//                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//                os = new ObjectOutputStream(fos);
//                os.writeObject(token);
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            finally {
//                try {
//                    os.close();
//                    fos.close();
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        public AccessToken loadAccessToken() {
//            FileInputStream fis = null;
//            ObjectInputStream is = null;
//            AccessToken token = null;
//
//            try {
//                fis = context.openFileInput(fileName);
//                is = new ObjectInputStream(fis);
//                token = (AccessToken) is.readObject();
//            }
//            catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            finally {
//                try {
//                    is.close();
//                    fis.close();
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return token;
//        }
//
//        public void clearAccessToken() {
//            storeAccessToken(null);
//        }
//    }
}
