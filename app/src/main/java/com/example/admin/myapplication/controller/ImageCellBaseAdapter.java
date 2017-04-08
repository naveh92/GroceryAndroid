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
import com.example.admin.myapplication.controller.handlers.BitmapReceivedHandler;
import com.example.admin.myapplication.controller.handlers.UserReceivedHandler;
import com.example.admin.myapplication.model.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 06/04/2017.
 */
public abstract class ImageCellBaseAdapter extends BaseAdapter {
    // TODO: Try to fix this
//    /**
//     * This Map represents the images we are CURRENTLY fetching from Storage.
//     * We hold it so that if there are 100 requests from the same user,
//     * we will only send a single request to the Storage.
//     *
//     * Key: userKey
//     * Value: List of all views that should be updated once the image was fetched.
//     */
//    private Map<String, List<UpdateView>> imagesToDownload = new HashMap<>();

    protected void initUserImageView(final String userKey, View cell) {
        final ImageView imageView = (ImageView)cell.findViewById(R.id.userImageView);
        final ProgressBar progressBar = (ProgressBar) cell.findViewById(R.id.pleaseWait);

        // Show the progress-bar and hide the imageView until the image returns from storage.
        imageView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

//        // Make sure we haven't already sent a request to the DB for this image.
//        if (!imagesToDownload.containsKey(userKey)) {
            // Retrieve the user image from storage.
            BitmapReceivedHandler receivedImageHandler = new BitmapReceivedHandler() {
                @Override
                public void onBitmapReceived(Bitmap bitmap) {
//                    List<UpdateView> viewsToUpdate = imagesToDownload.get(userKey);

//                    synchronized (viewsToUpdate) {
                        if (bitmap != null) {
                            DisplayMetrics dm = new DisplayMetrics();
                            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

//                            for (UpdateView updateView : viewsToUpdate) {
//                                ImageView imageView = updateView.getImageView();

                                // Set the metrics and image.
                                imageView.setMinimumHeight(dm.heightPixels);
                                imageView.setMinimumWidth(dm.widthPixels);
                                imageView.setImageBitmap(bitmap);
//                            }
                        }

//                        for (UpdateView updateView : viewsToUpdate) {
//                            ImageView imageView = updateView.getImageView();
//                            ProgressBar progressBar = updateView.getProgressBar();

                            // Show the imageView and hide the progress-bar
                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//
//                        // All views updated, so remove them.
//                        imagesToDownload.remove(userKey);
//                    }
                }

                @Override
                public void removeAllBitmaps() {}
            };

            // Retrieve the user image from storage.
            ImageDB.getInstance().downloadImage(userKey, receivedImageHandler);
//        }

//        // Add this cell's views to the list of views to update.
//        UpdateView updateView = new UpdateView(userImageView, pleaseWait);
//        List<UpdateView> viewsToUpdate = imagesToDownload.get(userKey);
//
//        if (viewsToUpdate == null) {
//            // Set the list of views to update
//            viewsToUpdate = new ArrayList<>();
//            imagesToDownload.put(userKey, viewsToUpdate);
//        }
//
//        synchronized (viewsToUpdate) {
//            viewsToUpdate.add(updateView);
//        }
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

//    /**
//     * This class represents all the views we need to update in a cell
//     * once an image has been fetched from storage.
//     */
//    private class UpdateView {
//        ImageView imageView;
//        ProgressBar progressBar;
//
//        public UpdateView(ImageView imageView, ProgressBar progressBar) {
//            this.imageView = imageView;
//            this.progressBar = progressBar;
//        }
//
//        public ImageView getImageView() { return imageView; }
//        public ProgressBar getProgressBar() { return progressBar; }
//    }
}
