package com.example.admin.myapplication.controller.group;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.TableViewFragment;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.group.members.GroupMembersTableActivity;
import com.example.admin.myapplication.controller.handlers.GroupReceivedHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupFragment extends TableViewFragment {
    private UserGroupsDB db;
    private GroupTableAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        // Save the add button for animations later
        addNewButton = (ImageButton) view.findViewById(R.id.add_new_object_button);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        adapter = new GroupTableAdapter(getActivity());
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Open an activity for the group that was clicked - show all members in it.
                Intent intent = new Intent(getActivity(), GroupMembersTableActivity.class);

                Group group = UserGroupsDB.getGroup(position);
                intent.putExtra("groupKey", group.getKey()); // Add the groupKey for the next activity.
                intent.putExtra("groupTitle", group.getTitle()); // Add the groupTitle for the next activity.

                startActivity(intent);
            }
        });

        fetchGroups();

        return view;
    }

    private void fetchGroups() {
        GroupReceivedHandler groupReceivedHandler = new GroupReceivedHandler() {
            @Override
            public void onGroupReceived(Group group) {
                // In case this happens on refresh when MainActivity is first created
                if (adapter != null) {
                    adapter.onGroupReceived(group);
                }
            }

            @Override
            public void removeAllGroups() {}
        };

        if (db == null) {
            db = new UserGroupsDB(AuthenticationManager.getInstance().getCurrentUserId());
        }
        db.observeUserGroupsAddition(groupReceivedHandler);
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_group_dialog);
        dialog.setTitle("New Group");

        // Get the EditText and focus on it.
        final EditText groupTitleText = (EditText) dialog.findViewById(R.id.groupTitleText);
        groupTitleText.requestFocus();

        ImageButton confirmButton = (ImageButton) dialog.findViewById(R.id.confirm);

        // If button is clicked, close the custom dialog
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // Get the user input.
                String groupTitle = groupTitleText.getText().toString();

                // Add the new group to the database.
                Group newGroup = new Group("", groupTitle);

                // Get userKey from Auth
                String userKey = AuthenticationManager.getInstance().getCurrentUserId();

                // Add the group
                String groupKey = GroupsDB.getInstance().addNewGroup(newGroup, userKey);

                // Add the user as a group member
                new GroupMembersDB(groupKey).addMember(userKey);

                // Make sure db is not null
                if (db == null) {
                    db = new UserGroupsDB(AuthenticationManager.getInstance().getCurrentUserId());
                }

                // Add the group to the users list of groups
                db.addGroupToUser(groupKey);
            }
        });

        dialog.show();
    }

    @Override
    protected void refresh() {
        fetchGroups();
    }
}