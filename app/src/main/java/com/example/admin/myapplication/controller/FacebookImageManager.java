package com.example.admin.myapplication.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;

import java.io.IOException;
import java.net.URL;

/**
 * Created by admin on 11/04/2017.
 */
public class FacebookImageManager {
    public void downloadUserProfilePic(final String facebookId, final ObjectReceivedHandler<Bitmap> handler) {

        // Create a new async-task so that we don't perform networking on the main thread.
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap userImage = null;

                URL imgUrl;

                try {
                    imgUrl = new URL("https://graph.facebook.com/" + facebookId + "/picture?type=large");
                    userImage = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());

                    handler.onObjectReceived(userImage);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                return userImage;
            }
        }.execute();
    }
}