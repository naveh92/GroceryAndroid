package com.example.admin.myapplication.controller.grocery.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.database.models.UserGroceryLitsModel;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.database.remote.UserGroceryListsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryListTableAdapter extends BaseAdapter {
    private Context mContext;
    UserGroceryLitsModel db;

    public GroceryListTableAdapter(Context c, UserGroceryLitsModel db) {
        mContext = c;
        this.db = db;
    }

    public int getCount() {
        return db.getListsCount();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the LayoutInflater from the Context.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.grocery_list_table_cell, parent, false);

        // Get the relevant grocery-list
        GroceryList list = db.getGroceryList(position);
        String listTitle = list.getTitle();

        // Get the Title TextView, and set its text.
        TextView listTitleTV = (TextView)view.findViewById(R.id.listTitle);
        listTitleTV.setText(listTitle);

        ObjectReceivedHandler<Group> groupReceivedHandler = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group group) {
                // Get the GroupName TextView, and set its text.
                TextView groupName = (TextView)view.findViewById(R.id.groupName);
                groupName.setText(group.getTitle());
            }
        };

        // Get this lists group, and when finished, set its title.
        GroupsDB.getInstance().findGroupByKey(list.getGroupKey(), groupReceivedHandler);

        // Get the PopupMenu button and set its id to the position.
        ImageView popupButton = (ImageView)view.findViewById(R.id.popupBtn);
        popupButton.setId(position);

        return view;
    }
}