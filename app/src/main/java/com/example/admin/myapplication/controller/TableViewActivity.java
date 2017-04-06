package com.example.admin.myapplication.controller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridView;
import android.widget.ImageButton;

/**
 * Created by admin on 05/04/2017.
 */
public abstract class TableViewActivity extends Activity {
    protected abstract void newObjectDialog(Context context);

    protected ImageButton addNewButton;
    private static long lastTouchTime = 0;
    private static boolean startedThread = false;
    private static final long delay = 1000;
    private static final int ACCELERATE_FACTOR = 4;
    private static final int DECELERATE_FACTOR = 2;

    protected void createHideViewsWhenScroll(GridView gridView) {
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                hideViews();
                lastTouchTime = System.currentTimeMillis();

                if (!startedThread) {

                    new AsyncTask<String, Void, Void>()
                    {
                        @Override
                        protected Void doInBackground(String... params)
                        {
                            while (System.currentTimeMillis() - lastTouchTime < delay) {}

                            showViews();
                            startedThread = false;
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {}

                        @Override
                        protected void onPreExecute() {}

                        @Override
                        protected void onProgressUpdate(Void... values) {}
                    }.execute();

                    startedThread = true;
                }


                return false;
            }
        });
    }



    private boolean showingViews = true;

    private void showViews() {
        if (!showingViews) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    addNewButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(DECELERATE_FACTOR)).start();
                    showingViews = true;
                }
            });
        }
    }

    private void hideViews() {
        if (showingViews) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    addNewButton.animate().translationY(addNewButton.getHeight() + addNewButton.getY()).setInterpolator(new AccelerateInterpolator(ACCELERATE_FACTOR)).start();
                    showingViews = false;
                }
            });
        }
    }
}
