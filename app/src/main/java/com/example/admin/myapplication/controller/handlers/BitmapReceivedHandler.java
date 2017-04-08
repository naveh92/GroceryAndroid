package com.example.admin.myapplication.controller.handlers;

import android.graphics.Bitmap;

/**
 * Created by admin on 08/04/2017.
 */
public interface BitmapReceivedHandler {
    void onBitmapReceived(Bitmap bitmap);
    void removeAllBitmaps();
}
