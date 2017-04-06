package com.example.admin.myapplication.controller.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.database.remote.ImageDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 06/04/2017.
 */
public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_view, container, false);

        // TODO: Get the userKey from Auth
        String userKey = "IBln4QIZm0TCveScQERgOcm0vBe2";

        initUsernameTextView(userKey, (TextView) view.findViewById(R.id.userNameTV));
        initImageView(userKey, (ImageView) view.findViewById(R.id.imageView));

        return view;
    }

    private void initUsernameTextView(String userKey, final TextView userNameTV) {
        ObjectReceivedHandler userReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                User user = (User) obj;
                userNameTV.setText(user.getName());
            }

            @Override
            public void removeAllObjects() {}
        };

        UsersDB.getInstance().findUserByKey(userKey, userReceivedHandler);
    }

    private void initImageView(String userKey, final ImageView imageView) {
        ObjectReceivedHandler imageReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                Bitmap bitmap = (Bitmap) obj;
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void removeAllObjects() {}
        };

        ImageDB.getInstance().downloadImage(userKey, imageReceivedHandler);
    }

    public void changeImageDialog(Context context) {
//        // Open a dialog.
//        final Dialog dialog = new Dialog(context);
//        dialog.setContentView(R.layout.new_group_dialog);
//        dialog.setTitle("New Group");
//
//        // Get the EditText and focus on it.
//        final EditText groupTitleText = (EditText) dialog.findViewById(R.id.groupTitleText);
//        groupTitleText.requestFocus();
//
//        ImageButton confirmButton = (ImageButton) dialog.findViewById(R.id.confirm);
//
//        // If button is clicked, close the custom dialog
//        confirmButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//
//                // Get the user input.
//                String groupTitle = groupTitleText.getText().toString();
//
//                // Add the new group to the database.
//                Group newGroup = new Group("", groupTitle);
//
//                // TODO: Get from Auth
//                String userKey = "IBln4QIZm0TCveScQERgOcm0vBe2";
//
//                // TODO: UserGroupsDB? GroupMembersDB?
//                GroupsDB.getInstance().addNewGroup(newGroup, userKey);
//            }
//        });
//
//        dialog.show();
    }
}