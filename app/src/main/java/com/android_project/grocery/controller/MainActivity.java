package com.android_project.grocery.controller;

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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.android_project.grocery.controller.authentication.AuthenticationManager;
import com.android_project.grocery.controller.database.local.DatabaseHelper;
import com.android_project.grocery.controller.database.models.LastUpdatedModel;
import com.android_project.grocery.controller.grocery.list.GroceryFragment;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.database.models.UserGroceryListsModel;
import com.android_project.grocery.controller.profile.ProfileFragment;
import com.facebook.login.LoginManager;

public class MainActivity extends FragmentActivity {
    private TabsPagerAdapter adapter;
    private ViewPager mViewPager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onDestroy() {
        DatabaseHelper.getInstance().close();
        // Destroy the instance instead of calling getInstance().destroy(),
        // because this creates an instance with null UserKey.
        UserGroceryListsModel.destroyInstance();

        // No need to destroy LastUpdatedModel because it's not an AbstractModel (No Listeners).

        super.onDestroy();
    }

    private void refreshTab() {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof TableViewFragment) {
            ((TableViewFragment) fragment).refresh();
        }
    }

    /**
     * -----------------
     * OnClick functions
     * -----------------
     */
    public void newObjectDialog(View view) {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof TableViewFragment) {
            ((TableViewFragment) fragment).newObjectDialog(this);
        }
    }

    public void changeImageDialog(View view) {
        Fragment fragment = adapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof ProfileFragment) {
            ((ProfileFragment)fragment).changeImageDialog(this);
        }
    }

    public void logout(View view) {
        LoginManager.getInstance().logOut();
        AuthenticationManager.getInstance().logOut();

        // Clear the access token cache.
        AuthenticationManager.getInstance().clearAccessToken();

        // Clear the LastUpdatedTable cache.
        LastUpdatedModel.getInstance().releaseCache();

        // Destroy the singleton instance that is associated with this user.
        UserGroceryListsModel.destroyInstance();

        // Once we logged-out, go back to LoginActivity.
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // Finish this activity.
        finish();
    }

    public void showListPopup(View v) {
        // Get the position of the row (list) that has been clicked.
        final int position = ((View) v.getParent()).getId();

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
