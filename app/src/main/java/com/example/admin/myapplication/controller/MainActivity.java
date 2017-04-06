package com.example.admin.myapplication.controller;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    TabsPagerAdapter adapter;
    ViewPager mViewPager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate called");

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        // Start the tab addition
        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        // Set a swipe-gesture recognizer.
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        // Add 3 tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab().setText("Groups").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Lists").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Profile").setTabListener(tabListener));

        // TODO: This should happen after login, and needs to be UserGroupsDB.
        // Create a handler and observe groups.
        ObjectReceivedHandler groupReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object group) {}

            @Override
            public void removeAllObjects() {}
        };

        GroupsDB.getInstance().observeGroupsAddition(groupReceivedHandler);
    }

    protected void newObjectDialog(View view) {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof TableViewFragment) {
            ((TableViewFragment) fragment).newObjectDialog(this);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart called");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart called");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
    }
}
