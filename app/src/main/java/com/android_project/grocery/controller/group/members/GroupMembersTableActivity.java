package com.android_project.grocery.controller.group.members;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android_project.grocery.controller.database.models.GroupMembersModel;
import com.android_project.grocery.model.entities.Group;
import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.TableViewActivity;
import com.android_project.grocery.controller.authentication.AuthenticationManager;
import com.android_project.grocery.controller.database.models.UserGroceryListsModel;
import com.android_project.grocery.controller.database.models.UserGroupsModel;
import com.android_project.grocery.controller.handlers.ObjectHandler;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;

/**
 * Created by admin on 06/04/2017.
 */
public class GroupMembersTableActivity extends TableViewActivity {
    private GroupMembersModel db;
    private String groupKey;
    private GroupMembersTableAdapter groupMembersAdapter;

    private final ObjectHandler<User> memberReceivedHandler = new ObjectHandler<User>() {
        @Override
        public void onObjectReceived(User member) {
            groupMembersAdapter.onMemberReceived(member);
        }

        @Override
        public void removeAllObjects() {
            groupMembersAdapter.removeAllMembers();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_view);

        // Retrieve the groupKey from the previous activity.
        Intent intent = getIntent();
        groupKey = intent.getStringExtra(Group.GROUP_KEY_STRING);
        String groupTitle = intent.getStringExtra(Group.TITLE_STRING);

        // Set this activity's title.
        setTitle(groupTitle);

        // Save the add button for animations later
        addNewButton = (ImageButton) findViewById(R.id.add_new_object_button);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        groupMembersAdapter = new GroupMembersTableAdapter(this);
        gridview.setAdapter(groupMembersAdapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        // Create a new GroupMembersDB specific to this Group.
        db = new GroupMembersModel(groupKey);
        fetchGroupMembers();
    }

    private void fetchGroupMembers() {
        db.observeGroupMembers(memberReceivedHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_leave: {
                // Show confirmation Dialog
                new AlertDialog.Builder(this).setTitle(getString(R.string.leave_group))
                        .setMessage(getString(R.string.are_you_sure_leave_group))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.leave, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Get userKey from Auth
                                String userKey = AuthenticationManager.getInstance().getCurrentUserId();

                                // Remove the user from the group.
                                db.removeMember(userKey);

                                ObjectReceivedHandler<GroceryList> listRemovedHandler = new ObjectReceivedHandler<GroceryList>() {
                                    @Override
                                    public void onObjectReceived(GroceryList obj) {}
                                };
                                // Get the UserGroceryListsModel of this user and call removeGroupLists()?
                                UserGroceryListsModel.getInstance().removeGroupLists(groupKey, listRemovedHandler);

                                // Remove the group from the users list of groups.
                                new UserGroupsModel(AuthenticationManager.getInstance().getCurrentUserId()).removeGroupFromUser(groupKey);

                                // Mock a back press so that we exit this activity and go back to the list of groups.
                                GroupMembersTableActivity.this.onBackPressed();
                            }})
                        .setNegativeButton(R.string.stay, null).show();

                break;
            }

            default:
                break;
        }

        return true;
    }

    public void newObjectDialog(View view) {
        this.newObjectDialog(this);
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_member_dialog);
        dialog.setTitle(context.getString(R.string.add_a_member));

        GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
        final GroupMembersTableAdapter newMembersAdapter = new GroupMembersTableAdapter(this);
        gridview.setAdapter(newMembersAdapter);

        final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    dialog.dismiss();

                    // Get the selected user
                    User user = (User) newMembersAdapter.getItem(position);

                    // Add the member to the group.
                    db.addMember(user.getKey());

                    // Add the group to the users list of groups
                    new UserGroupsModel(user.getKey()).addGroupToUser(groupKey);

                    // Refresh the group members
                    fetchGroupMembers();
            }
        });

        ObjectHandler<User> facebookFriendHandler = new ObjectHandler<User>() {
            @Override
            public void onObjectReceived(User user) {
                newMembersAdapter.onMemberReceived(user);

                // Received a new member - hide the progress bar.
                hideProgressBar(progressBar);
            }

            @Override
            public void removeAllObjects() {
                newMembersAdapter.removeAllMembers();
            }
        };

        ObjectReceivedHandler<Boolean> whenFinished = new ObjectReceivedHandler<Boolean>() {
            @Override
            public void onObjectReceived(Boolean noFriendsToAdd) {
                if (noFriendsToAdd) {
                    // Show alert dialog
                    new AlertDialog.Builder(GroupMembersTableActivity.this).setTitle(getString(R.string.sorry))
                            .setMessage(getString(R.string.no_members_to_add))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int whichButton) {
                                    // Dismiss both dialogs.
                                    alertDialog.dismiss();
                                    dialog.dismiss();
                                }}).show();

                    // No members - hide the progress bar.
                    hideProgressBar(progressBar);
                }
            }
        };

        // Retrieve relevant users from Facebook
        new FacebookFriendsFinder().find(groupMembersAdapter.getAllMembers(), facebookFriendHandler, whenFinished);

        dialog.show();
    }

    private void hideProgressBar(ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}