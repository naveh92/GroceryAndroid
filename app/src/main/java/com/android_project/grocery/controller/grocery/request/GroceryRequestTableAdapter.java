package com.android_project.grocery.controller.grocery.request;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android_project.grocery.controller.ImageCellBaseAdapter;
import com.android_project.grocery.model.entities.GroceryRequest;
import com.android_project.grocery.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequestTableAdapter extends ImageCellBaseAdapter {
    private final Context mContext;
    private final LayoutInflater inflater;
    private static List<GroceryRequest> groceryRequests = new ArrayList<>();

    // Editing
    private String editingRequestKey = null;
    private String newItemName = null;
    private Boolean edited = false;

    public GroceryRequestTableAdapter(Context c) {
        mContext = c;

        // Get the LayoutInflater from the Context.
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grocery_request_table_cell, parent, false);
        }
        final View view = convertView;

        // Get the relevant grocery-request
        GroceryRequest request = groceryRequests.get(position);

        // Check if editing mode is on for this cell
        if (editingRequestKey != null && editingRequestKey.equals(request.getKey())) {
            // Editing mode is on for this cell.
            initEditingMode(request, view);
        }
        else {
            // Editing mode is off.
            initNonEditingMode(request, view);
        }

        // Initialize the views
        super.initUserNameTextView(request.getUserKey(), (TextView)view.findViewById(R.id.userName));
        // Use cache to retrieve the image, if available.
        super.initUserImageView(request.getUserKey(), view, true);

        return view;
    }

    private void initEditingMode(final GroceryRequest request, View cell) {
        final String requestItemName = request.getItemName();

        ImageButton confirmButton = (ImageButton) cell.findViewById(R.id.v);
        final EditText editText = (EditText) cell.findViewById(R.id.editText);

        if (!editText.getText().toString().equals(requestItemName) && !edited) {
            editText.setText(requestItemName);
            newItemName = requestItemName;
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                edited = true;
                newItemName = editText.getText().toString();
            }
        });

        // Hide and show the relevant views
        confirmButton.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        cell.findViewById(R.id.itemName).setVisibility(View.INVISIBLE);

        // Focus on the editText and select all text.
        editText.selectAll();
        editText.requestFocus();
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
        if (request != null) {
            // Check if this is an update and not a new request being added.
            Integer index = getIndex(request.getKey());
            if (index != null) {
                groceryRequests.set(index, request);
            }
            else {
                groceryRequests.add(request);
            }

            Collections.sort(groceryRequests);
        }

        // notifyDataSetChanged queries all data again, so just use Invalidated.
        super.notifyDataSetInvalidated();
    }

    private Integer getIndex(String key) {
        for (int i = 0; i<groceryRequests.size(); i++) {
            GroceryRequest current = groceryRequests.get(i);
            if (current != null && current.getKey() != null && current.getKey().equals(key)) {
                return i;
            }
        }
        return null;
    }

    public void removeAllRequests() {
        groceryRequests.clear();
    }

    public GroceryRequest getRequest(int position) {
        return groceryRequests.get(position);
    }

    public void startEditing(int index) {
        editingRequestKey = getRequest(index).getKey();

        // notifyDataSetChanged queries all data again, so just use Invalidated.
        super.notifyDataSetInvalidated();
    }

    public void stopEditing() {
        editingRequestKey = null;
        newItemName = null;
        edited = false;

        // notifyDataSetChanged queries all data again, so just use Invalidated.
        super.notifyDataSetInvalidated();
    }

    public String getNewItemName() {
        return newItemName;
    }

    public String getEditingRequestKey() {
        return editingRequestKey;
    }
}