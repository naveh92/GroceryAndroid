package com.example.admin.myapplication.controller.database.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.example.admin.myapplication.controller.handlers.BitmapReceivedHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 05/04/2017.
 */
public class ImageDB {
    private static final String TAG = "ImageDB";
    private static final String FORMAT_SUFFIX = ".jpg";
    private static ImageDB instance;
    private StorageReference storageRef;
    private Map<String, Bitmap> cache = new HashMap<>();

    private ImageDB() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }
    public static ImageDB getInstance() {
        if (instance == null) {
            instance = new ImageDB();
        }

        return instance;
    }

    public void downloadImage(String userKey, BitmapReceivedHandler handler) {
        String imagePath = userKey + FORMAT_SUFFIX;

        // TODO: Make sure the image is up to date.
        boolean imageUpToDate = cache.containsKey(userKey);

        if (imageUpToDate) {
            // TODO: Get the image from the local storage
            Bitmap bitmap = cache.get(userKey);
            handler.onBitmapReceived(bitmap);
        }
        else {
            getImageFromRemote(imagePath, handler);
        }
    }

    private void getImageFromRemote(String imagePath, final BitmapReceivedHandler handler) {
        final long ONE_MEGABYTE = 1024 * 1024;

        // TODO: Delete this when saving to local storage.
        final String userKey = imagePath.substring(0, imagePath.length() - FORMAT_SUFFIX.length());

        storageRef.child(imagePath).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for requested image returns
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                handler.onBitmapReceived(bitmap);

                // TODO: Save the image to local storage
                cache.put(userKey, bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                handler.onBitmapReceived(null);
            }
        });
    }

//    // Array of callback functions. Observers insert their functions here, and are notified when the user changes his image.
//    static var callbacks: Array<()->()> = []
//
//    private init() {
//        configureStorage()
//    }
//
//    func configureStorage() {
//        let storageUrl = FIRApp.defaultApp()?.options.storageBucket
//                storageRef = FIRStorage.storage().reference(forURL: "gs://" + storageUrl!)
//    }
//
//    static func observeImageModification(whenImageModified: @escaping () -> ()) {
//        callbacks.append(whenImageModified)
//    }
//
//    private static func executeCallbacks() {
//        for callback in callbacks {
//            callback()
//        }
//    }
//
//    func storeImage(image: UIImage, userId: String) {
//        storeImage(image: image, userId: userId, whenFinished: {
//            // Notify the observers
//            ImageDB.executeCallbacks()
//        })
//    }
//
//    func storeImage(image: UIImage, userId: String, whenFinished: @escaping ()->()) {
//        // Don't store the default picture
//        if (!(ImageDB.defaultImage!.isEqual(image))) {
//            let imageData = UIImageJPEGRepresentation(image, 0.5)
//            let imagePath = "\(userId).jpg"
//
//            let metadata = FIRStorageMetadata()
//            metadata.contentType = "image/jpeg"
//
//            self.storageRef?.child(imagePath).put(imageData!, metadata: metadata) {(metadata, error) in
//                if let error = error {
//                    print("Error uploading: \(error)")
//                    return
//                }
//
//                // Save the image to local storage
//                LocalImageStorage.sharedInstance.saveImageToFile(image: image, name: imagePath);
//
//                whenFinished()
//            }
//        }
//    }
//
//
//    private func manageRefreshImage(imagePath: String, whenFinished: @escaping (UIImage?) -> Void) {
//        // Create reference to the file whose metadata we want to retrieve
//        let imageRef = self.storageRef?.child(imagePath)
//
//        // Get metadata properties
//        imageRef?.metadata { metadata, error in
//            if let error = error {
//                // An error occurred!
//                print ("Error getting update time: \(error)")
//                return
//            }
//            else {
//                // Metadata now contains the metadata for the image
//                self.compareUpdateTimes(imagePath: imagePath, remoteUpdateTime: metadata?.updated, whenFinished: whenFinished)
//            }
//        }
//    }
//
//    private func compareUpdateTimes(imagePath: String, remoteUpdateTime: Date?, whenFinished: @escaping (UIImage?) -> Void) {
//        let localUpdateTime = LocalImageStorage.sharedInstance.getUpdateTime(path: imagePath)
//
//        // Check if the remote image was updated
//        if (remoteUpdateTime != nil &&
//                (localUpdateTime == nil || localUpdateTime?.compare(remoteUpdateTime!) == .orderedAscending)) {
//            // Download the new remote image
//            getImageFromRemote(imagePath: imagePath, whenFinished: whenFinished)
//        }
//        else {
//            return
//        }
//    }

}