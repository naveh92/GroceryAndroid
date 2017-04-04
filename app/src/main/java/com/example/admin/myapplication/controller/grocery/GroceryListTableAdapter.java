package com.example.admin.myapplication.controller.grocery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.model.container.Groups;
import com.example.admin.myapplication.model.entities.GroceryList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryListTableAdapter extends BaseAdapter {
    private static List<GroceryList> groceryLists = new ArrayList<>();
    private Context mContext;

    public GroceryListTableAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return groceryLists.size();
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
        View view = inflater.inflate(R.layout.grocery_list_table_cell, parent, false);

        // Get the relevant grocery-list
        GroceryList list = groceryLists.get(position);

        // Get the Title TextView, and set its text.
        TextView listTitle = (TextView)view.findViewById(R.id.listTitle);
        listTitle.setText(list.getTitle());

        // Get this lists group title
        String groupTitle = Groups.title(list.getGroupKey());

        // Get the GroupName TextView, and set its text.
        TextView groupName = (TextView)view.findViewById(R.id.groupName);
        groupName.setText(groupTitle);

        return view;
    }

    public void onListReceived(GroceryList list) {
        groceryLists.add(list);
        notifyDataSetChanged();
    }

    public void removeAllLists() {
        groceryLists.clear();
    }
}