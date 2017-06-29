package com.example.admin.myapplication.controller.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;

import com.example.admin.myapplication.R;

/**
 * Created by admin on 08/04/2017.
 */
public class ChangeImageDialog extends Dialog {
    private static final int CAMERA_ACTION_CODE = 0;
    private static final int GALLERY_ACTION_CODE = 1;
    Activity activity;

    public ChangeImageDialog(Context context) {
        super(context);

        activity = (Activity)context;

        setContentView(R.layout.change_image_dialog);
        setTitle(context.getString(R.string.choose_media));

        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraBtn);
        ImageButton galleryButton = (ImageButton) findViewById(R.id.galleryBtn);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                dismiss();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                dismiss();
            }
        });
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        activity.startActivityForResult(pickPhoto, GALLERY_ACTION_CODE);
    }

    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        activity.startActivityForResult(takePicture, CAMERA_ACTION_CODE);
    }
}
