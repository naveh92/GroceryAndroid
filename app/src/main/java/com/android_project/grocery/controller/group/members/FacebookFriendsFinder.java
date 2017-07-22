package com.android_project.grocery.controller.group.members;

import android.os.Bundle;

import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.controller.authentication.AuthenticationManager;
import com.android_project.grocery.controller.database.models.UsersModel;
import com.android_project.grocery.controller.handlers.ObjectHandler;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by admin on 07/04/2017.
 * This class finds all facebook members of the current User, which could be added to a group.
 */
public class FacebookFriendsFinder {
    private static final String FIELDS_KEY = "fields";
    private static final String FIELDS_VALUE = "id, name";
    private static final String PATH = "/me/friends";
    private static final String DATA_KEY = "data";
    private static final String ID_KEY = "id";

    /**
     * This function gets the array of users from the facebook GraphRequest,
     * Iterates over all of them, and fetches the User object, while maintaining a counter:
     *  Everytime a User object is received, the counter is decreased.
     *  When the counter finally reaches 0, indicating that we have finished receiving all requested User objects, we notify the whenFinishedHandler.
     * @param currentMembers - Current list of members in the group.
     * @param memberReceived - Handler for when a potential User was received.
     * @param whenFinishedHandler - Handler for when we finished receiving all users. (Checks if there were users - if not, raises an alert "No more users to add..")
     */
    public void find(final List<User> currentMembers, final ObjectHandler<User> memberReceived, final ObjectReceivedHandler<Boolean> whenFinishedHandler) {
        Bundle parameters = new Bundle();
        parameters.putString(FIELDS_KEY, FIELDS_VALUE);

        GraphRequest request = new GraphRequest(AuthenticationManager.getInstance().getFacebookAccessToken(),
                                                PATH, parameters, null, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                memberReceived.removeAllObjects();

                // Final array for access from an inner class later.
                final Boolean[] noFriendsToAdd = {true};
                JSONArray users = null;
                try {
                    users = (JSONArray) response.getJSONObject().get(DATA_KEY);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                if (users != null) {
                    // Final array for access in an inner class.
                    // Create a counter for the number of users we still need to check.
                    final int[] numberOfUsersNotYetReceived = {users.length()};

                    for (int i = 0; i < users.length(); i++) {
                        JSONObject currentUser;
                        String currentUserFacebookId = "";

                        try {
                            currentUser = users.getJSONObject(i);
                            currentUserFacebookId = currentUser.getString(ID_KEY);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Make sure this user isn't already a member in the group
                        if (!containsFacebookId(currentMembers, currentUserFacebookId)) {
                            // Mark that there are members ONLY when we get a member.
                            ObjectReceivedHandler<User> userReceived = new ObjectReceivedHandler<User>() {
                                @Override
                                public void onObjectReceived(User member) {
                                    // Only if an actual member has been returned.
                                    if (member != null) {
                                        noFriendsToAdd[0] = false;
                                        memberReceived.onObjectReceived(member);
                                    }

                                    // Decrease the count of users remaining - we finished this user.
                                    numberOfUsersNotYetReceived[0]--;

                                    // Check if we are not waiting for any more users ( = Finished iterating over all potential users).
                                    checkIfFinished(numberOfUsersNotYetReceived[0], noFriendsToAdd[0], whenFinishedHandler);
                                }
                            };

                            // Find the User object corresponding to this facebook-user.
                            UsersModel.getInstance().findUserByFacebookId(currentUserFacebookId, userReceived);
                        }
                        else {
                            // This user is already in the group - doesn't count.
                            numberOfUsersNotYetReceived[0]--;
                        }
                    }
                    // Check if we are not waiting for any more users ( = All potential users are already in the group).
                    checkIfFinished(numberOfUsersNotYetReceived[0], noFriendsToAdd[0], whenFinishedHandler);
                }
                else {
                    // There were no users, so we are not waiting for any.
                    checkIfFinished(0, noFriendsToAdd[0], whenFinishedHandler);
                }
            }
        });

        // Execute the request.
        request.executeAsync();
    }

    /**
     * This function is called when we want to know if we are waiting for any other users from the RemoteDB.
     * @param numberOfPotentialMember - Number of users we are waiting for.
     * @param noFriendsToAdd - Boolean value indicating whether there were users until now or not.
     * @param whenFinishedHandler - The handler that is called when the find() process is completely finished.
     */
    private void checkIfFinished(int numberOfPotentialMember, Boolean noFriendsToAdd, ObjectReceivedHandler<Boolean> whenFinishedHandler) {
        // Check if this was the last user (= we aren't waiting for any more users to return).
        if (numberOfPotentialMember == 0) {
            // Notify the caller whether there are friends to add
            whenFinishedHandler.onObjectReceived(noFriendsToAdd);
        }
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