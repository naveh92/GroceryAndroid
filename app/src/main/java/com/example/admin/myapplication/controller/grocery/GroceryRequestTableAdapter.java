package com.example.admin.myapplication.controller.grocery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.model.container.Groups;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.GroceryRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequestTableAdapter extends BaseAdapter {
    private static List<GroceryRequest> groceryRequests = new ArrayList<>();
    private Context mContext;

    public GroceryRequestTableAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return groceryRequests.size();
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
        View view = inflater.inflate(R.layout.grocery_request_table_cell, parent, false);

        // Get the relevant grocery-request
        GroceryRequest request = groceryRequests.get(position);

        // Get the Title TextView, and set its text.
        TextView itemName = (TextView)view.findViewById(R.id.itemName);
        itemName.setText(request.getItemName());

        if (request.getPurchased()) {
            // Strike it out and make it gray.
            itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            itemName.setTextColor(Color.GRAY);

        }
        else {
            itemName.setPaintFlags(0);
            itemName.setTextColor(Color.WHITE);
        }

        // TODO: Go to userDB and retrieve it.
        // Get this requests userKey
        String userKey = request.getUserKey();

        String userName = "Get this from UserDB!"; //UserDB.getUserName(userKey);

        // Get the userName TextView, and set its text.
        TextView userNameTV = (TextView)view.findViewById(R.id.userName);
        userNameTV.setText(userName);

        // TODO: Get the user Image from Storage.

        return view;
    }

    public void onRequestReceived(GroceryRequest request) {
        groceryRequests.add(request);
        notifyDataSetChanged();
    }

    public void removeAllRequests() {
        groceryRequests.clear();
    }

    public GroceryRequest getRequest(int position) {
        return groceryRequests.get(position);
    }
}