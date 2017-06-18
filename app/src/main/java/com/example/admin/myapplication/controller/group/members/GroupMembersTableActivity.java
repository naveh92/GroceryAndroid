package com.example.admin.myapplication.controller.group.members;

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

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.TableViewActivity;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.models.UserGroupsModel;
import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 06/04/2017.
 */
public class GroupMembersTableActivity extends TableViewActivity {
    private GroupMembersDB db;
    private String groupKey;
    private GroupMembersTableAdapter groupMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_view);

        // Retrieve the groupKey from the previous activity.
        Intent intent = getIntent();
        groupKey = intent.getStringExtra("groupKey");
        String groupTitle = intent.getStringExtra("groupTitle");

        // Set this activity's title.
        setTitle(groupTitle);

        // Save the add button for animations later
        addNewButton = (ImageButton) findViewById(R.id.add_new_object_button);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        groupMembersAdapter = new GroupMembersTableAdapter(this);
        gridview.setAdapter(groupMembersAdapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        ObjectHandler<User> memberReceivedHandler = new ObjectHandler<User>() {
            @Override
            public void onObjectReceived(User member) {
                groupMembersAdapter.onMemberReceived(member);
            }

            @Override
            public void removeAllObjects() {
                groupMembersAdapter.removeAllMembers();
            }
        };

        // Create a new GroupMembersDB specific to this Group.
        db = new GroupMembersDB(groupKey);
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
                // TODO: Strings.xml
                // Show confirmation Dialog
                new AlertDialog.Builder(this).setTitle("Leave group")
                        .setMessage("Are you sure?\nYou will be able to rejoin only if another member adds you back.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.leave, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Get userKey from Auth
                                String userKey = AuthenticationManager.getInstance().getCurrentUserId();

                                // Remove the user from the group.
                                db.removeMember(userKey);

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

    protected void newObjectDialog(View view) {
        this.newObjectDialog(this);
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_member_dialog);
        dialog.setTitle("Add a member");

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
                    // TODO: Strings.xml
                    // Show alert dialog
                    new AlertDialog.Builder(GroupMembersTableActivity.this).setTitle("Sorry!")
                            .setMessage("There are no more members to add to this group.")
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