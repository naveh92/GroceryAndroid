package com.example.admin.myapplication.controller.grocery.request;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ImageCellBaseAdapter;
import com.example.admin.myapplication.model.entities.GroceryRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequestTableAdapter extends ImageCellBaseAdapter {
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

    @Override
    protected Context getContext() { return mContext; }

    // Create a new cell for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the LayoutInflater from the Context.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.grocery_request_table_cell, parent, false);

        // Get the relevant grocery-request
        GroceryRequest request = groceryRequests.get(position);

        // Initialize the views
        initItemNameTextView(request, (TextView)view.findViewById(R.id.itemName));
        super.initUserNameTextView(request.getUserKey(), (TextView)view.findViewById(R.id.userName));
        super.initUserImageView(request.getUserKey(), view);

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

    public void onRequestReceived(GroceryRequest request) {
        groceryRequests.add(request);
        Collections.sort(groceryRequests);
        notifyDataSetChanged();
    }

    public void removeAllRequests() {
        groceryRequests.clear();
    }

    public GroceryRequest getRequest(int position) {
        return groceryRequests.get(position);
    }
}