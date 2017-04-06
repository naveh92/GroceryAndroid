package com.example.admin.myapplication.controller.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupTableAdapter extends BaseAdapter {
    private Context mContext;

    public GroupTableAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return GroupsDB.getInstance().groupsNum();
    }

    public Object getItem(int position) {
        return GroupsDB.getInstance().getGroup(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the LayoutInflater from the Context.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_table_cell, parent, false);

        Group currentGroup = GroupsDB.getInstance().getGroup(position);

        // Get the Title TextView, and set its text.
        TextView listTitle = (TextView)view.findViewById(R.id.listTitle);
        listTitle.setText(currentGroup.getTitle());

        return view;
    }

    public void onGroupReceived(Group group) {
        // What if this happens before Groups is notified?
        notifyDataSetChanged();
    }
}