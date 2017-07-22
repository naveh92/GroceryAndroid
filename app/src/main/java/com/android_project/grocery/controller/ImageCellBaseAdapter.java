package com.android_project.grocery.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android_project.grocery.controller.database.models.ImageModel;
import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.database.models.UsersModel;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 06/04/2017.
 */
public abstract class ImageCellBaseAdapter extends BaseAdapter {
    /**
     * In-memory cache for retrieving images.
     * If we don't have the image we are looking for, we fetch it.
     * If we do, we return it from the cache.
     */
    private Map<String, Bitmap> images = new HashMap<>();

    /**
     * This function fetches the ImageView from ImageModel, and updates the UI accordingly.
     */
    protected void initUserImageView(final String userKey, View cell) {
        final ImageView imageView = (ImageView)cell.findViewById(R.id.userImageView);
        final ProgressBar progressBar = (ProgressBar) cell.findViewById(R.id.pleaseWait);
        final ObjectReceivedHandler<Bitmap> receivedImageHandler = new ObjectReceivedHandler<Bitmap>() {
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
