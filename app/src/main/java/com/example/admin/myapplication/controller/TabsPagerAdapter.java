package com.example.admin.myapplication.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.admin.myapplication.controller.grocery.GroceryFragment;
import com.example.admin.myapplication.controller.group.GroupFragment;

/**
 * Created by admin on 04/04/2017.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            // TODO: Enum & Ordinals
            case (0):
            {
                return new GroupFragment();
            }
            case (1):
            {
                return new GroceryFragment();
            }
            case (2):
            {
                return new GroceryFragment();
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        // TODO: Enum.values.groupsNum();
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}