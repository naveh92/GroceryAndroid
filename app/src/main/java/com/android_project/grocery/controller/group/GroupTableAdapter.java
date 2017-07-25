package com.android_project.grocery.controller.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android_project.grocery.controller.database.models.GroupMembersModel;
import com.android_project.grocery.controller.database.models.UserGroupsModel;
import com.android_project.grocery.controller.handlers.ObjectHandler;
import com.android_project.grocery.model.entities.Group;
import com.android_project.grocery.model.entities.User;
import com.android_project.grocery.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupTableAdapter extends BaseAdapter {
    private static final int DESCRIPTION_MAX_CHAR_NUM = 25;
    private static final String SPACE = " ";
    private static String ONE_MEMBER;
    private static String NO_MEMBERS;
    private static String MEMBERS;
    private static String LEFT_SOGER;
    private static String RIGHT_SOGER;
    private static String DELIMITER;
    private static String THREE_DOTS;

    private final Context mContext;
    private final LayoutInflater inflater;
    private UserGroupsModel db;
    private List<GroupMembersModel> groupMembersModels = new ArrayList<>();

    public GroupTableAdapter(Context c, UserGroupsModel db, GroupMembersModel groupMembersDB) {
        mContext = c;
        // Get the LayoutInflater from the Context.
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.db = db;

        initStrings();
    }

    private void initStrings() {
        ONE_MEMBER = mContext.getString(R.string.one_member);
        NO_MEMBERS = mContext.getString(R.string.no_members);
        MEMBERS = mContext.getString(R.string.members);
        LEFT_SOGER = mContext.getString(R.string.left_soger);
        RIGHT_SOGER = mContext.getString(R.string.right_soger);
        DELIMITER = mContext.getString(R.string.description_limiter);
        THREE_DOTS = mContext.getString(R.string.three_dots);
    }

    public int getCount() {
        return db.getGroupsCount();
    }
    public Object getItem(int position) {
        return null;
    }
    public long getItemId(int position) {
        return 0;
    }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_table_cell, parent, false);
        }
        final View view = convertView;

        /**
         * -------------
         *     Title
         * -------------
         */
        Group currentGroup = db.getGroup(position);

        // Get the Title TextView, and set its text.
        TextView groupTitle = (TextView)view.findViewById(R.id.groupTitle);
        groupTitle.setText(currentGroup.getTitle());

        /**
         * -------------
         *  Description
         * -------------
         */
        // Get the Description TextView, and reset its text.
        final TextView groupDescription = (TextView)view.findViewById(R.id.description);
        groupDescription.setText("");

        // Create a list, holding the members of this group.
        final List<String> members = new ArrayList<>();

        // Retrieve the member count
        ObjectHandler<User> memberReceivedHandler = new ObjectHandler<User>() {
            @Override
            public void removeAllObjects() {
                synchronized (members) {
                    members.clear();
                    refreshDescription();
                }
            }

            @Override
            public void onObjectReceived(User member) {
                synchronized (members) {
                    members.add(member.getName());
                    refreshDescription();
                }
            }

            private void refreshDescription() {
                String text = getDescriptionText(members);
                groupDescription.setText(text);
            }
        };

        // TODO: Try a cached-like map so we don't create new every time?
        GroupMembersModel currentGroupModel = new GroupMembersModel(currentGroup.getKey());
        currentGroupModel.observeGroupMembers(memberReceivedHandler);
        groupMembersModels.add(currentGroupModel);
        return view;
    }

    private String getDescriptionText(List<String> members) {
        StringBuilder sb = new StringBuilder();

        if (members.size() == 1) {
            String memberName = getFirstName(members.get(0));
            sb.append(ONE_MEMBER);
            if (sb.length() + memberName.length() < DESCRIPTION_MAX_CHAR_NUM) {
                sb.append(SPACE).append(LEFT_SOGER).append(memberName).append(RIGHT_SOGER);
            }
        }
        else if (members.size() > 0) {
            sb.append(members.size()).append(SPACE).append(MEMBERS);

            String firstMemberName = getFirstName(members.get(0));
            if (sb.length() + firstMemberName.length() < DESCRIPTION_MAX_CHAR_NUM) {
                sb.append(SPACE).append(LEFT_SOGER).append(firstMemberName);

                for (int i=1; i<members.size(); i++) {
                    String memberName = getFirstName(members.get(i));
                    if (sb.length() < DESCRIPTION_MAX_CHAR_NUM) {
                        sb.append(DELIMITER).append(SPACE).append(memberName);
                    } else {
                        sb.append(THREE_DOTS);
                        break;
                    }
                }

                sb.append(RIGHT_SOGER);
            }
        }
        else {
            sb.append(NO_MEMBERS);
        }

        return sb.toString();
    }

    private String getFirstName(String fullName) {
        if (fullName != null) {
            String[] names = fullName.split(SPACE);

            if (names.length > 0) {
                return names[0];
            }
        }
        return fullName;
    }

    public void onGroupReceived() {
        notifyDataSetChanged();
    }
    public void onDestroy() {
        for (GroupMembersModel model : groupMembersModels) {
            model.destroy();
        }
    }
}