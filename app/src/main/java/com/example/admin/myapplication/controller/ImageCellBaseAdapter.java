package com.example.admin.myapplication.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.database.models.ImageModel;
import com.example.admin.myapplication.controller.database.models.UsersModel;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 06/04/2017.
 */
public abstract class ImageCellBaseAdapter extends BaseAdapter {
    private Map<String, Bitmap> images = new HashMap<>();

    protected void initUserImageView(final String userKey, View cell) {
        final ImageView imageView = (ImageView)cell.findViewById(R.id.userImageView);
        final ProgressBar progressBar = (ProgressBar) cell.findViewById(R.id.pleaseWait);

            ObjectReceivedHandler<Bitmap> receivedImageHandler = new ObjectReceivedHandler<Bitmap>() {
                @Override
                public void onObjectReceived(Bitmap bitmap) {
                    if (imageView != null) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);

                            // Save this bitmap for later in case we try to get it again.
                            images.put(userKey, bitmap);
                        }

                        // Show the imageView and hide the progress-bar
                        imageView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            };

        // Check if this is the first time getting the relevant images.
        if (!images.containsKey(userKey)) {
            // Retrieve the user image from storage.
            ImageModel.getInstance().downloadImage(getContext(), userKey, receivedImageHandler);

            // Register this callback for when the image changes.
            ImageModel.getInstance().registerCallback(receivedImageHandler);
        }
        else {
            // Pass the image we previously received.
            receivedImageHandler.onObjectReceived(images.get(userKey));
        }
    }

    protected void initUserNameTextView(String userKey, final TextView userNameTV) {
        // Retrieve the user object from the DB.
        ObjectReceivedHandler<User> receivedUserHandler = new ObjectReceivedHandler<User>() {
            @Override
            public void onObjectReceived(User user) {
                if (user != null) {
                    // Get the userName TextView, and set its text.
                    String userName = user.getName();
                    userNameTV.setText(userName);
                }
            }
        };

        // Retrieve the user object from the DB.
        UsersModel.getInstance().findUserByKey(userKey, receivedUserHandler);
    }

    protected abstract Context getContext();
}
