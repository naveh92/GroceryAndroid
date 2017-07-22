package com.android_project.grocery.controller.group.members;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.ImageCellBaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 05/04/2017.
 */
public class GroupMembersTableAdapter extends ImageCellBaseAdapter {
    private final List<User> members = new ArrayList<>();
    private Context mContext;

    public GroupMembersTableAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return members.size();
    }

    public Object getItem(int position) {
        if (position < members.size()) {
            return members.get(position);
        }

        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    protected Context getContext() { return mContext; }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the LayoutInflater from the Context.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.group_member_table_cell, parent, false);

        // Get the relevant member
        User user = members.get(position);

        // Initialize the views
        super.initUserNameTextView(user.getKey(), (TextView)view.findViewById(R.id.userName));
        super.initUserImageView(user.getKey(), view);

        return view;
    }

    public void onMemberReceived(User member) {
        members.add(member);
        Collections.sort(members);
        notifyDataSetChanged();
    }

    public void removeAllMembers() {
        members.clear();
    }

    public List<User> getAllMembers() {
        // Create an unmodifiable copy of the members.
        return Collections.unmodifiableList(members);
    }
}