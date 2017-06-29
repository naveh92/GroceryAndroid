package com.example.admin.myapplication.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.admin.myapplication.controller.grocery.list.GroceryFragment;
import com.example.admin.myapplication.controller.group.GroupFragment;
import com.example.admin.myapplication.controller.profile.ProfileFragment;

/**
 * Created by admin on 04/04/2017.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i < getCount()) {
            TabsEnum tab = TabsEnum.values()[i];

            switch (tab) {
                case GROUPS: {
                    return new GroupFragment();
                }
                case GROCERY: {
                    return new GroceryFragment();
                }
                case PROFILE: {
                    return new ProfileFragment();
                }
                default: {
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public int getCount() {
        return TabsEnum.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}