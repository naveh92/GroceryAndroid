package com.example.admin.myapplication.controller.grocery.request;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.database.remote.UsersDB;
import com.example.admin.myapplication.model.entities.GroceryRequest;
import com.example.admin.myapplication.model.entities.User;

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
        final View view = inflater.inflate(R.layout.grocery_request_table_cell, parent, false);

        // Get the relevant grocery-request
        GroceryRequest request = groceryRequests.get(position);

        // Initialize the views
        initItemNameTextView(request, (TextView)view.findViewById(R.id.itemName));
        initUserNameTextView(request.getUserKey(), (TextView)view.findViewById(R.id.userName));
        // TODO: Get the user Image from Storage.
//        initUserImage(request.getUserKey(), (ImageView)view.findViewById(R.id.userImage));

        return view;
    }

    private void initItemNameTextView(GroceryRequest request, TextView itemNameTV) {
        // Set its text.
        itemNameTV.setText(request.getItemName());

        if (request.getPurchased()) {
            // Strike it out and make it gray.
            itemNameTV.setPaintFlags(itemNameTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            itemNameTV.setTextColor(Color.GRAY);

        }
        else {
            itemNameTV.setPaintFlags(0);
            itemNameTV.setTextColor(Color.WHITE);
        }
    }

    private void initUserNameTextView(String userKey, final TextView userNameTV) {
        // Retrieve the user object from the DB.
        ObjectReceivedHandler receivedUserHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                // Get the userName TextView, and set its text.
                String userName = ((User)obj).getName();
                userNameTV.setText(userName);
            }

            @Override
            public void removeAllObjects() {}
        };

        // Retrieve the user object from the DB.
        UsersDB.getInstance().findUserByKey(userKey, receivedUserHandler);
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