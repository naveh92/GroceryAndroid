package com.example.admin.myapplication.controller.group.members;

import android.os.Bundle;

import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.models.UsersModel;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by admin on 07/04/2017.
 */
public class FacebookFriendsFinder {
    private static final String FIELDS = "id, name";
    private static final String PATH = "/me/friends";

    public void find(final List<User> currentMembers, final ObjectHandler<User> memberReceived, final ObjectReceivedHandler<Boolean> whenFinishedHandler) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", FIELDS);

        GraphRequest request = new GraphRequest(AuthenticationManager.getInstance().getFacebookAccessToken(),
                                                PATH, parameters, null, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                memberReceived.removeAllObjects();

                Boolean noFriendsToAdd = true;
                // TODO: Switch case on success code?
                JSONArray users = null;
                try {
                    users = (JSONArray) response.getJSONObject().get("data");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject currentUser = null;
                        String currentUserFacebookId = "";

                        try {
                            currentUser = users.getJSONObject(i);
                            currentUserFacebookId = currentUser.getString("id");
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Make sure this user isn't already a member in the group
                        if (!containsFacebookId(currentMembers, currentUserFacebookId)) {
                            // Find the User object corresponding to this facebook-user.
                            UsersModel.getInstance().findUserByFacebookId(currentUserFacebookId, memberReceived);
                            noFriendsToAdd = false;
                        }
                    }
                }

                // Notify the caller whether there are friends to add
                // (If there are, the caller should be waiting..)
                whenFinishedHandler.onObjectReceived(noFriendsToAdd);
            }
        });

        // Execute the request.
        request.executeAsync();
    }

    private boolean containsFacebookId(List<User> currentMembers, String currentUserFacebookId) {
        for (User user : currentMembers) {
            if (user.getFacebookId().equals(currentUserFacebookId)) {
                return true;
            }
        }

        return false;
    }
}