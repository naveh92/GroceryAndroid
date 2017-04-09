package com.example.admin.myapplication.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.database.remote.ImageDB;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.handlers.UserReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

/**
 * Created by admin on 06/04/2017.
 */
public abstract class ImageCellBaseAdapter extends BaseAdapter {
    // TODO: Try to fix this so that we don't download the same image 34548694267 times.

    protected void initUserImageView(final String userKey, View cell) {
        final ImageView imageView = (ImageView)cell.findViewById(R.id.userImageView);
        final ProgressBar progressBar = (ProgressBar) cell.findViewById(R.id.pleaseWait);

        // Show the progress-bar and hide the imageView until the image returns from storage.
        imageView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

            ObjectReceivedHandler<Bitmap> receivedImageHandler = new ObjectReceivedHandler<Bitmap>() {
                @Override
                public void onObjectReceived(Bitmap bitmap) {
                    if (imageView != null) {
                        if (bitmap != null) {
                            // TODO: Is this needed??????
                            DisplayMetrics dm = new DisplayMetrics();
                            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

                            // Set the metrics and image.
                            imageView.setMinimumHeight(dm.heightPixels);
                            imageView.setMinimumWidth(dm.widthPixels);
                            imageView.setImageBitmap(bitmap);
                        }

                        // Show the imageView and hide the progress-bar
                        imageView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void removeAllObjects() {}
            };

            // Retrieve the user image from storage.
            ImageDB.getInstance().downloadImage(getContext(), userKey, receivedImageHandler);

            // Register this callback for when the image changes.
            ImageDB.getInstance().registerCallback(receivedImageHandler);
    }

    protected void initUserNameTextView(String userKey, final TextView userNameTV) {
        // Retrieve the user object from the DB.
        UserReceivedHandler receivedUserHandler = new UserReceivedHandler() {
            @Override
            public void onUserReceived(User user) {
                if (user != null) {
                    // Get the userName TextView, and set its text.
                    String userName = user.getName();
                    userNameTV.setText(userName);
                }
            }

            @Override
            public void removeAllUsers() {}
        };

        // Retrieve the user object from the DB.
        UsersDB.getInstance().findUserByKey(userKey, receivedUserHandler);
    }

    protected abstract Context getContext();
}
