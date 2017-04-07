package com.example.admin.myapplication.controller.group.members;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.TableViewActivity;
import com.example.admin.myapplication.controller.database.remote.GroupMembersDB;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 06/04/2017.
 */
public class GroupMembersTableActivity extends TableViewActivity {
    private GroupMembersDB db;
    private String groupKey;

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
        final GroupMembersTableAdapter adapter = new GroupMembersTableAdapter(this);
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        ObjectReceivedHandler memberReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object member) {
                adapter.onMemberReceived((User) member);
            }

            @Override
            public void removeAllObjects() {
                adapter.removeAllMembers();
            }
        };

        // Create a new GroupMembersDB specific to this Group.
        db = new GroupMembersDB(groupKey);
        db.observeGroupMembers(memberReceivedHandler);
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
        final GroupMembersTableAdapter adapter = new GroupMembersTableAdapter(this);
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    dialog.dismiss();

                    // Get the selected user
                    User user = (User) adapter.getItem(position);

                    // Add the member to the group.
                    db.addMember(user.getKey());

                    // TODO: Register the group in the member's  list of group.
//                new UserGroupsDB(user.getKey()).addGroup(groupKey);
            }
        });

        ObjectReceivedHandler facebookFriendHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                User user = (User) obj;
                adapter.onMemberReceived(user);
            }

            @Override
            public void removeAllObjects() {
                adapter.removeAllMembers();
            }
        };

        // Retrieve relevant users from Facebook
//        FacebookFriendsFinder
        db.observeGroupMembers(facebookFriendHandler);

        dialog.show();
    }
}