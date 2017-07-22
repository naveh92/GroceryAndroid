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
    private final Context mContext;
    private final LayoutInflater inflater;
    private final List<User> members = new ArrayList<>();

    public GroupMembersTableAdapter(Context c) {
        mContext = c;

        // Get the LayoutInflater from the Context.
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return members.size();
    }
    public long getItemId(int position) {
        return 0;
    }
    public Object getItem(int position) {
        if (position < members.size()) {
            return members.get(position);
        }

        return null;
    }

    @Override
    protected Context getContext() { return mContext; }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_member_table_cell, parent, false);
        }
        final View view = convertView;

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