package com.android_project.grocery.controller.group;

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

import com.android_project.grocery.controller.TableViewFragment;
import com.android_project.grocery.controller.database.models.GroupMembersModel;
import com.android_project.grocery.controller.database.models.GroupsModel;
import com.android_project.grocery.controller.group.members.GroupMembersTableActivity;
import com.android_project.grocery.model.entities.Group;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.authentication.AuthenticationManager;
import com.android_project.grocery.controller.database.models.UserGroupsModel;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;

/**
 * Created by admin on 04/04/2017.
 * Groups fragment tab
 */
public class GroupFragment extends TableViewFragment {
    private UserGroupsModel db;
    private GroupTableAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        // Save the add button for animations later
        addNewButton = (ImageButton) view.findViewById(R.id.add_new_object_button);

        // Make sure the DB is not null & it's this users instance
        manageDBInstance();

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        adapter = new GroupTableAdapter(getActivity(), db);
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Open an activity for the group that was clicked - show all members in it.
                Intent intent = new Intent(getActivity(), GroupMembersTableActivity.class);

                Group group = db.getGroup(position);
                intent.putExtra(Group.GROUP_KEY_STRING, group.getKey()); // Add the groupKey for the next activity.
                intent.putExtra(Group.TITLE_STRING, group.getTitle()); // Add the groupTitle for the next activity.

                startActivity(intent);
            }
        });

        refresh();

        return view;
    }

    /**
     * This function makes sure that the DB instance is not null,
     * and that the instance is the correct instance for the current user
     * (In case we logged-off and logged-in to a different user)
     */
    private void manageDBInstance() {
        String currentUserKey = AuthenticationManager.getInstance().getCurrentUserId();

        if (db == null || db.getUserKey() == null || !db.getUserKey().equals(currentUserKey)) {
            db = new UserGroupsModel(currentUserKey);
        }
    }

    private void fetchGroups() {
        ObjectReceivedHandler<Group> groupReceivedHandler = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group group) {
                // In case this happens on refresh when MainActivity is first created
                if (adapter != null) {
                    adapter.onGroupReceived();
                }
            }
        };

        // Make sure the DB is not null & it's this users instance
        manageDBInstance();
        db.observeUserGroupsAddition(groupReceivedHandler);
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_group_dialog);
        dialog.setTitle(context.getString(R.string.new_group));

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
                String groupKey = GroupsModel.getInstance().addNewGroup(newGroup);

                // Add the user as a group member
                // NOTE: No need to destroy() this model, because it doesn't have any Listeners (We only added a member).
                new GroupMembersModel(groupKey).addMember(userKey);

                // Make sure the DB is not null & it's this users instance
                manageDBInstance();

                // Add the group to the users list of groups
                db.addGroupToUser(groupKey);
            }
        });

        dialog.show();
    }

    @Override
    protected void refresh() {
        notifyDataSetChanged();
        fetchGroups();
    }

    @Override
    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        adapter.onDestroy();
        db.destroy();
        super.onDestroy();
    }
}