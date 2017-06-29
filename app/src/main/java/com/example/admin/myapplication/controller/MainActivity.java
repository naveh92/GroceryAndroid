package com.example.admin.myapplication.controller;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.grocery.list.GroceryFragment;
import com.example.admin.myapplication.controller.profile.ProfileFragment;
import com.facebook.login.LoginManager;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private TabsPagerAdapter adapter;
    private ViewPager mViewPager;

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
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

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

                        // Refresh the grocery tab if its the tab that was selected.
                        // (In case we left a group that contains lists.. the lists should be removed).
                        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());
                        if (fragment instanceof TableViewFragment) {
                            ((TableViewFragment) fragment).notifyDataSetChanged();
                        }
                    }
                });

        // Add 3 tabs, specifying the tab's text and TabListener
        if (actionBar != null) {
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.groups)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.lists)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.profile)).setTabListener(tabListener));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshTab();
    }

    private void refreshTab() {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof TableViewFragment) {
            ((TableViewFragment) fragment).refresh();
        }
    }

    protected void newObjectDialog(View view) {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof TableViewFragment) {
            ((TableViewFragment) fragment).newObjectDialog(this);
        }
    }

    protected void changeImageDialog(View view) {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof ProfileFragment) {
            ((ProfileFragment)fragment).changeImageDialog(this);
        }
    }

    protected void logout(View view) {
        LoginManager.getInstance().logOut();
        AuthenticationManager.getInstance().logOut();

        // Clear the access token cache.
        AuthenticationManager.getInstance().clearAccessToken();

        // Once we logged-out, go back to LoginActivity.
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // Finish this activity.
        finish();
    }

    protected void showListPopup(View v) {
        // Get the position of the list that has been clicked.
        final int position = v.getId();

        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.list_options, popup.getMenu());

        // Registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    // action with ID action_refresh was selected
                    case R.id.action_delete_list:
                    {
                        // Show confirmation Dialog
                        new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.delete_grocery_list))
                                .setMessage(getString(R.string.are_you_sure))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Get the current fragment
                                        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

                                        if (fragment instanceof GroceryFragment) {
                                            ((GroceryFragment) fragment).deleteList(position);
                                        }
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();

                        break;
                    }

                    default:
                        break;
                }

                return true;
            }
        });

        popup.show();
    }

    /**
     * This is for camera/gallery image pick
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Uri selectedImageUri = null;

        if (resultCode == RESULT_OK) {
            selectedImageUri = imageReturnedIntent.getData();
        }

        // Get the current fragment
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof ProfileFragment) {
            ((ProfileFragment) fragment).refreshImage(this, selectedImageUri);
        }
    }
}
