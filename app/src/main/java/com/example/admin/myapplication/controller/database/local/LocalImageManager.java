package com.example.admin.myapplication.controller.database.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by admin on 09/04/2017.
 */
public class LocalImageManager {
    private static final String TAG = "LocalImageManager";
    private static final String FORMAT_SUFFIX = ".jpg";

    public Bitmap loadImageFromStorage(Context context, String userKey) {
        Bitmap bitmapFromFile = null;
        File file = getMediaFile(context, userKey);

        try {
            bitmapFromFile = BitmapFactory.decodeStream(new FileInputStream(file));
            Log.d(TAG, "Successfully loaded image " + getImageName(userKey) + " from local storage.");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmapFromFile;
    }

    public void saveToInternalStorage(Context context, String userKey, Bitmap bitmap, int compressionFactor) {
        File file = getMediaFile(context, userKey);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, compressionFactor, fos);
            Log.d(TAG, "Successfully saved image " + getImageName(userKey) + " to local storage.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Create a File for saving an image or video */
    private  File getMediaFile(Context context, String userKey) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String mImageName = getImageName(userKey);
        return new File(mediaStorageDir.getPath() + File.separator + mImageName);
    }

    private String getImageName(String userKey) {
        return userKey + FORMAT_SUFFIX;
    }

    public Long getUpdateTime(Context context, String userKey) {
        Long lastUpdated = null;
        File file = getMediaFile(context, userKey);

        if (file != null && file.exists()) {
            lastUpdated = file.lastModified();
        }

        return lastUpdated;
    }
}
