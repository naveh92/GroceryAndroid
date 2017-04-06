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
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.TableViewFragment;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.group.members.GroupMembersTableActivity;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupFragment extends TableViewFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        // Save the add button for animations later
        addNewButton = (ImageButton) view.findViewById(R.id.add_new_object_button);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        final GroupTableAdapter adapter = new GroupTableAdapter(getActivity());
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Open an activity for the group that was clicked - show all members in it.
                Intent intent = new Intent(getActivity(), GroupMembersTableActivity.class);

                Group group = (Group) adapter.getItem(position);
                intent.putExtra("groupKey", group.getKey()); // Add the groupKey for the next activity.
                intent.putExtra("groupTitle", group.getTitle()); // Add the groupTitle for the next activity.

                startActivity(intent);
            }
        });

        ObjectReceivedHandler groupReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object group) {
                adapter.onGroupReceived((Group) group);
            }

            @Override
            public void removeAllObjects() {}
        };

        GroupsDB.getInstance().observeGroupsAddition(groupReceivedHandler);

        return view;
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

                // TODO: Get from Auth
                String userKey = "IBln4QIZm0TCveScQERgOcm0vBe2";

                // TODO: UserGroupsDB? GroupMembersDB?
                GroupsDB.getInstance().addNewGroup(newGroup, userKey);
            }
        });

        dialog.show();
    }
}