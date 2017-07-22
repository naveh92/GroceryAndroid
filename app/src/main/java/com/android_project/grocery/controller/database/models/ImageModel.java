package com.android_project.grocery.controller.database.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android_project.grocery.controller.database.local.files.LocalImageManager;
import com.android_project.grocery.controller.database.remote.ImageDB;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 17/07/2017.
 *
 * This Model's execution:
 *
 * - Fetching:
 *      Fetches the remote update-time of the image.
 *          If it is after the local update-time, we fetch from remote and save to local.
 *          Else, we fetch from local.
 *
 * - Storing:
 *      Stores in remote.
 *      When finished, re-downloads it to get a new reference (That will not be recycled),
 *      When the image is re-downloaded, notifies all the registered callbacks and passes the images to them.
 *      Finally, stores in local.
 */
public class ImageModel {
    private static final String TAG = "ImageModel";
    private static final String FORMAT_SUFFIX = ".jpg";
    private static final int COMPRESSION_FACTOR = 25;
    private static ImageModel instance;

    private final LocalImageManager localImageManager = new LocalImageManager();

    /**
     * These handlers are notified when the user changes his image.
     * These are the adapters for various list-views over the app.
     */
    private final List<ObjectReceivedHandler<Bitmap>> callbacks = new ArrayList<>();

    public static ImageModel getInstance() {
        if (instance == null) {
            instance = new ImageModel();
        }
        return instance;
    }

    /**
     * ------------------------
     *     Image Retrieval
     * ------------------------
     */

    /**
     * This function starts the image download process.
     * It gets the remote image's metadata, and later syncs the local and remote images.
     * If the image is synchronized, it is then passed to handler.
     */
    public void downloadImage(final Context context, final String userKey, final ObjectReceivedHandler<Bitmap> handler) {
        final String imageName = getImageName(userKey);

        if (context != null) {
            ObjectReceivedHandler<Long> remoteUpdateTimeHandler = new ObjectReceivedHandler<Long>() {
                @Override
                public void onObjectReceived(Long updateTime) {
                    // After we got the remote update time, compare it to local update time.
                    compareUpdateTimes(context, userKey, updateTime, handler);
                }
            };

            // Fetch the remote update time of the image from the RemoteDB.
            ImageDB.getInstance().fetchRemoteUpdateTime(imageName, remoteUpdateTimeHandler);
        }
        else {
            // Context is null - blindly fetch image from RemoteDB.
            ImageDB.getInstance().getImageFromRemote(imageName, handler);
        }
    }
    /**
     * This function performs the process of syncing the local image with the remote one.
     * When the image is synchronized, it is then passed to handler.
     */
    private void compareUpdateTimes(final Context context, final String userKey, Long remoteUpdateTime, final ObjectReceivedHandler<Bitmap> handler) {
        final String imageName = getImageName(userKey);
        final Long localUpdateTime = localImageManager.getUpdateTime(context, imageName);

        // If remoteUpdateTime is null we don't have a remote image
        if (remoteUpdateTime != null) {
            // Check if the local image needs to be updated
            // If localUpdateTime is null we don't have a local image
            if (localUpdateTime == null || localUpdateTime < remoteUpdateTime) {
                Log.d(TAG, "Need to update image " + getImageName(userKey));

                ObjectReceivedHandler<Bitmap> bitmapReceivedFromRemoteHandler = new ObjectReceivedHandler<Bitmap>() {
                    @Override
                    public void onObjectReceived(Bitmap bitmap) {
                        Log.d(TAG, "Got image from remote: " + imageName);
                        handler.onObjectReceived(bitmap);

                        if (context != null && bitmap != null) {
                            Log.d(TAG, "Storing to local: " + imageName);

                            // After fetching the image from remote,
                            // Save it to local storage
                            localImageManager.saveToInternalStorage(context, userKey, imageName, bitmap, COMPRESSION_FACTOR);
                        }
                    }
                };

                // Download the new remote image
                ImageDB.getInstance().getImageFromRemote(imageName, bitmapReceivedFromRemoteHandler);
            }
            else if (localUpdateTime >= remoteUpdateTime) {
                // Image is up-to-date. Load it from local storage
                Bitmap bitmap = localImageManager.loadImageFromStorage(context, userKey, imageName);
                handler.onObjectReceived(bitmap);
            }
        }
        else {
            // No image in remote..
            handler.onObjectReceived(null);
        }
    }

    /**
     * ------------------------
     *     Image Storing
     * ------------------------
     */
    /**
     * This function stores the image to the remote db.
     * When finished, it re-downloads the image to get a new reference (That will not be recycled).
     * When the image is re-downloaded, it notifies all the registered callbacks and passes the images to them.
     */
    public void storeImage(final Context context, final Bitmap bitmap, final String userKey) {
        final String imageName = getImageName(userKey);
        final ObjectReceivedHandler<Object> imageStoredInRemoteHandler = new ObjectReceivedHandler<Object>() {
            @Override
            public void onObjectReceived(Object obj) {
                // We have stored the image, and it will be recycled later.
                // Re-download the image (to get a new reference that won't be recycled later).
                notifyCallbacks(context, userKey);

                // Save the image to local storage
                localImageManager.saveToInternalStorage(context, userKey, imageName, bitmap, COMPRESSION_FACTOR);
            }
        };

        ImageDB.getInstance().storeImage(bitmap, imageName, COMPRESSION_FACTOR, imageStoredInRemoteHandler);
    }

    /**
     * ------------------------
     *        Callbacks
     * ------------------------
     */
    public void registerCallback(ObjectReceivedHandler<Bitmap> handler) {
        callbacks.add(handler);
    }
    /**
     * This function re-downloads the image (to get a new reference that won't be recycled later),
     * and notifies all registered callbacks.
     */
    private void notifyCallbacks(final Context context, final String userKey) {
        final String imageName = getImageName(userKey);

        ObjectReceivedHandler<Bitmap> imageReceivedFromRemote =  new ObjectReceivedHandler<Bitmap>() {
            @Override
            public void onObjectReceived(Bitmap newUserBitmap) {
                Log.d(TAG, "Got image from remote: " + imageName);

                if (context != null && newUserBitmap != null) {
                    Log.d(TAG, "Storing to local: " + imageName);

                    // After fetching the image from remote,
                    // Save it to local storage
                    localImageManager.saveToInternalStorage(context, userKey, imageName, newUserBitmap, COMPRESSION_FACTOR);
                }

                // When finished and got the new image from remote, notify all callbacks.
                synchronized (callbacks) {
                    for (ObjectReceivedHandler<Bitmap> handler : callbacks) {
                        handler.onObjectReceived(newUserBitmap);
                    }
                }
            }
        };

        // Send the request
        ImageDB.getInstance().getImageFromRemote(imageName, imageReceivedFromRemote);
    }

    /**
     * ------------------------
     *      Help Functions
     * ------------------------
     */
    private static String getImageName(String userKey) {
        return userKey + FORMAT_SUFFIX;
    }
}
