package com.android_project.grocery.controller.grocery.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android_project.grocery.controller.database.models.GroupsModel;
import com.android_project.grocery.model.entities.Group;
import com.android_project.grocery.R;
import com.android_project.grocery.controller.database.models.UserGroceryListsModel;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryListTableAdapter extends BaseAdapter {
    private final LayoutInflater inflater;

    public GroceryListTableAdapter(Context c) {
        // Get the LayoutInflater from the Context.
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return UserGroceryListsModel.getInstance().getListsCount();
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
            convertView = inflater.inflate(R.layout.grocery_list_table_cell, parent, false);
        }
        final View view = convertView;

        // Get the relevant grocery-list
        GroceryList list = UserGroceryListsModel.getInstance().getGroceryList(position);
        String listTitle = list.getTitle();

        // Get the Title TextView, and set its text.
        TextView listTitleTV = (TextView) view.findViewById(R.id.listTitle);
        listTitleTV.setText(listTitle);

        ObjectReceivedHandler<Group> groupReceivedHandler = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group group) {
                // Get the GroupName TextView, and set its text.
                TextView groupName = (TextView) view.findViewById(R.id.groupName);
                groupName.setText(group.getTitle());
            }
        };

        // Get this lists group, and when finished, set its title.
        GroupsModel.getInstance().findGroupByKey(list.getGroupKey(), groupReceivedHandler);

        // Set its id to the position of the list,
        // so that when we call the delete function in MainActivity, we know what list to delete.
        view.setId(position);

        return view;
    }
}