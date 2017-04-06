package com.example.admin.myapplication.controller.grocery.request;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;


/**
 * Created by admin on 06/04/2017.
 */
public class GroupComboBoxAdapter<Group> extends ArrayAdapter<Group> {
    public GroupComboBoxAdapter(Context context, int resource, List<Group> groups) {
        super(context, resource, groups);
    }
}
