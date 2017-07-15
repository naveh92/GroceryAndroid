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
    private static Fragment[] tabs;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);

        if (tabs == null) {
            initTabs();
        }
    }

    private void initTabs() {
        tabs = new Fragment[getCount()];

        tabs[TabsEnum.GROUPS.ordinal()] = new GroupFragment();
        tabs[TabsEnum.GROCERY.ordinal()] = new GroceryFragment();
        tabs[TabsEnum.PROFILE.ordinal()] = new ProfileFragment();
    }

    @Override
    public Fragment getItem(int i) {
        if (i >= 0 && i < getCount()) {
            return tabs[i];
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