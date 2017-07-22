package com.android_project.grocery.controller.database.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by admin on 05/04/2017.
 *
 * This DB Stores/Fetches images to/from firebase remote db.
 */
public class ImageDB {
    private static final String TAG = "ImageDB";
    private static ImageDB instance;
    private StorageReference storageRef;

    private ImageDB() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }
    public static ImageDB getInstance() {
        if (instance == null) {
            instance = new ImageDB();
        }
        return instance;
    }

    public void fetchRemoteUpdateTime(final String imageName, final ObjectReceivedHandler<Long> handler) {
        // Make sure the image is up to date - get its metadata.
        storageRef.child(imageName).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for the image
                Long updateTime = storageMetadata.getUpdatedTimeMillis();
                handler.onObjectReceived(updateTime);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(TAG, "Failed to get metadata for image " + imageName);
                handler.onObjectReceived(null);
            }
        });
    }

    public void getImageFromRemote(final String imageName, final ObjectReceivedHandler<Bitmap> handler) {
        final long ONE_MEGABYTE = 1024 * 1024;

        storageRef.child(imageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Pass the received bitmap to the Model
                handler.onObjectReceived(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                handler.onObjectReceived(null);
            }
        });
    }

    public void storeImage(final Bitmap bitmap, final String imageName, final int compressionFactor, final ObjectReceivedHandler<Object> handler) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressionFactor, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child(imageName).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failed to upload image: " + imageName);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "New image successfully uploaded: " + imageName);

                // We have stored the image in remote db.
                // Notify the handler.
                handler.onObjectReceived(null);
            }
        });
    }
}