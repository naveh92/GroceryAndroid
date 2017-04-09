package com.example.admin.myapplication.controller.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.remote.ImageDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.handlers.UserReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 06/04/2017.
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_view, container, false);

        // Get userKey from Auth
        String userKey = AuthenticationManager.getInstance().getCurrentUserId();
        imageView = (ImageView) view.findViewById(R.id.imageView);

        initUsernameTextView(userKey, (TextView) view.findViewById(R.id.userNameTV));
        initImageView(userKey);

        return view;
    }

    private void initUsernameTextView(String userKey, final TextView userNameTV) {
        UserReceivedHandler userReceivedHandler = new UserReceivedHandler() {
            @Override
            public void onUserReceived(User user) {
                userNameTV.setText(user.getName());
            }

            @Override
            public void removeAllUsers() {}
        };

        UsersDB.getInstance().findUserByKey(userKey, userReceivedHandler);
    }

    private void initImageView(String userKey) {
        ObjectReceivedHandler<Bitmap> imageReceivedHandler = new ObjectReceivedHandler<Bitmap>() {
            @Override
            public void onObjectReceived(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }

                // TODO: Spinner?
            }

            @Override
            public void removeAllObjects() {}
        };

        ImageDB.getInstance().downloadImage(getContext(), userKey, imageReceivedHandler);
    }

    public void changeImageDialog(Context context) {
        // Open the dialog.
        final ChangeImageDialog dialog = new ChangeImageDialog(context);
        dialog.show();
    }

    public void refreshImage(Context context, Uri selectedImageUri) {
        if (selectedImageUri != null && imageView != null) {
            imageView.setImageURI(selectedImageUri);

            // Get the data from an ImageView as bytes
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();

            Bitmap bitmap = imageView.getDrawingCache();

            // Save the new image to the DB
            ImageDB.getInstance().storeImage(context, bitmap, AuthenticationManager.getInstance().getCurrentUserId());
        }
    }
}