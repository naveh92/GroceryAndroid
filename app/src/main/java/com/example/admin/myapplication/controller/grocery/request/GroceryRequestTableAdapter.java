package com.example.admin.myapplication.controller.grocery.request;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
    // Editing
    private Integer editingIndex = null;
    private String newItemName = null;

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

        // Check if editing mode is on for this cell
        if (editingIndex != null && editingIndex.equals(position)) {
            // Editing mode is on for this cell.
            initEditingMode(request, view);
        }
        else {
            // Editing mode is off.
            initNonEditingMode(request, view);
        }

        // Initialize the views
        super.initUserNameTextView(request.getUserKey(), (TextView)view.findViewById(R.id.userName));
        super.initUserImageView(request.getUserKey(), view);

        return view;
    }

    private void initEditingMode(GroceryRequest request, View cell) {
        ImageButton confirmButton = (ImageButton) cell.findViewById(R.id.v);
        final EditText editText = (EditText) cell.findViewById(R.id.editText);

        editText.setText(request.getItemName());

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                newItemName = editText.getText().toString();
                return false;
            }
        });

        // Hide and show the relevant views
        confirmButton.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        cell.findViewById(R.id.itemName).setVisibility(View.INVISIBLE);
    }

    private void initNonEditingMode(GroceryRequest request, View cell) {
        TextView itemNameTV = (TextView) cell.findViewById(R.id.itemName);

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

        // Hide and show the relevant views
        itemNameTV.setVisibility(View.VISIBLE);
        cell.findViewById(R.id.v).setVisibility(View.INVISIBLE);
        cell.findViewById(R.id.editText).setVisibility(View.INVISIBLE);
    }

    public void onRequestReceived(GroceryRequest request) {
        groceryRequests.add(request);
        Collections.sort(groceryRequests);

        // TODO: Invalidated or changed?
        // TODO: Changed queries everything again..
        super.notifyDataSetInvalidated();
//        notifyDataSetChanged();
    }

    public void removeAllRequests() {
        groceryRequests.clear();
    }

    public GroceryRequest getRequest(int position) {
        return groceryRequests.get(position);
    }

    public void startEditing(int index) {
        editingIndex = index;

        // TODO: Invalidated or changed?
        // TODO: Changed queries everything again..
        super.notifyDataSetInvalidated();
    }

    public void stopEditing() {
        editingIndex = null;
        newItemName = null;

        // TODO: Invalidated or changed?
        // TODO: Changed queries everything again..
        super.notifyDataSetInvalidated();
    }

    public String getNewItemName() {
        return newItemName;
    }

    public String getEditingRequestKey() {
        return getRequest(editingIndex).getKey();
    }
}