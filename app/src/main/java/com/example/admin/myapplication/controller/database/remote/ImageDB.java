package com.example.admin.myapplication.controller.database.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.admin.myapplication.controller.database.local.LocalImageManager;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 05/04/2017.
 */
public class ImageDB {
    private static final String TAG = "ImageDB";
    private static final String FORMAT_SUFFIX = ".jpg";
    private static final int COMPRESSION_FACTOR = 25;
    private static ImageDB instance;
    private StorageReference storageRef;
    private final LocalImageManager localImageManager = new LocalImageManager();

    /**
     * These handlers are notified when the user changes his image.
     * These are the adapters for various list-views over the app.
     */
    private final List<ObjectReceivedHandler<Bitmap>> callbacks = new ArrayList<>();

    private ImageDB() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }
    public static ImageDB getInstance() {
        if (instance == null) {
            instance = new ImageDB();
        }

        return instance;
    }

    /**
     * This function gets the remote image's metadata, and later syncs the local and remote images.
     * When the image is synchronized, it is then passed to handler.
     */
    public void downloadImage(final Context context, final String userKey, final ObjectReceivedHandler<Bitmap> handler) {
        final String imagePath = getImageName(userKey);

        if (context != null) {
            // Make sure the image is up to date - get its metadata.
            storageRef.child(imagePath).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for the image
                    Long updateTime = storageMetadata.getUpdatedTimeMillis();
                    compareUpdateTimes(context, userKey, updateTime, handler);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "Failed to get metadata for image " + imagePath);
                    handler.onObjectReceived(null);
                }
            });
        }
        else {
            getImageFromRemote(context, imagePath, handler);
        }
    }

    /**
     * This function performs the process of syncing the local image with the remote one.
     * When the image is synchronized, it is then passed to handler.
     */
    private void compareUpdateTimes(Context context, String userKey, Long remoteUpdateTime, final ObjectReceivedHandler<Bitmap> handler) {
        Long localUpdateTime = localImageManager.getUpdateTime(context, userKey);

        // If remoteUpdateTime is null we don't have a remote image
        if (remoteUpdateTime != null) {
            // Check if the local image needs to be updated
            // If localUpdateTime is null we don't have a local image
            if (localUpdateTime == null || localUpdateTime < remoteUpdateTime) {
                Log.d(TAG, "Need to update image " + getImageName(userKey));

                // Download the new remote image
                getImageFromRemote(context, userKey, handler);
            }
            else if (localUpdateTime >= remoteUpdateTime) {
                // Load the image from local storage
                Bitmap bitmap = localImageManager.loadImageFromStorage(context, userKey);
                handler.onObjectReceived(bitmap);
            }
        }
        else {
            // No image in remote..
            handler.onObjectReceived(null);
        }
    }

    private void getImageFromRemote(final Context context, final String userKey, final ObjectReceivedHandler<Bitmap> handler) {
        final long ONE_MEGABYTE = 1024 * 1024;
        final String imagePath = getImageName(userKey);

        storageRef.child(imagePath).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                Log.d(TAG, "Got image from remote: " + imagePath);
                handler.onObjectReceived(bitmap);

                if (context != null && bitmap != null) {
                    // Save the image to local storage
                    localImageManager.saveToInternalStorage(context, userKey, bitmap, COMPRESSION_FACTOR);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                handler.onObjectReceived(null);
            }
        });
    }

    public void storeImage(final Context context, final Bitmap bitmap, final String userKey) {
        final String imagePath = getImageName(userKey);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_FACTOR, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child(imagePath).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failed to upload image: " + imagePath);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "New image successfully uploaded: " + imagePath);

                // Re-download the image (to get a new reference that won't be recycled later).
                notifyCallbacks(context, userKey);

                // Save the image to local storage
                localImageManager.saveToInternalStorage(context, userKey, bitmap, COMPRESSION_FACTOR);
            }
        });
    }

    public void registerCallback(ObjectReceivedHandler<Bitmap> handler) {
        callbacks.add(handler);
    }

    /**
     * This function re-downloads the image (to get a new reference that won't be recycled later),
     * and notifies all registered callbacks.
     */
    private void notifyCallbacks(Context context, String userKey) {
        ObjectReceivedHandler<Bitmap> imageReceivedFromRemote =  new ObjectReceivedHandler<Bitmap>() {
            @Override
            public void onObjectReceived(Bitmap newUserBitmap) {
                // When finished and got the new image from remote, notify all callbacks.
                synchronized (callbacks) {
                    for (ObjectReceivedHandler<Bitmap> handler : callbacks) {
                        handler.onObjectReceived(newUserBitmap);
                    }
                }
            }

            @Override
            public void removeAllObjects() {}
        };

        // Send the request
        this.getImageFromRemote(context, userKey, imageReceivedFromRemote);
    }

    private String getImageName(String userKey) {
        return userKey + FORMAT_SUFFIX;
    }
}