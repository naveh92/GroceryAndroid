package com.example.admin.myapplication.controller.group;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.TableView;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.database.remote.RemoteDatabaseManager;
import com.example.admin.myapplication.controller.grocery.GroupComboBoxAdapter;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupFragment extends Fragment implements TableView {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        final GroupTableAdapter adapter = new GroupTableAdapter(getActivity());
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO: Open activity for the group that was clicked. need to show all members in it.
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