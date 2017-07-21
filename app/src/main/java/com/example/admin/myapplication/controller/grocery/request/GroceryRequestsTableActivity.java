package com.example.admin.myapplication.controller.grocery.request;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.TableViewActivity;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.models.RequestsModel;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.GroceryRequest;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequestsTableActivity extends TableViewActivity {
    private RequestsModel db;
    private GroceryRequestTableAdapter adapter;

    // TODO: REMOVE OBSERVERS ON onDestroy(). also in GroupMembers and any other activity.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_view);

        // Retrieve the listKey from the previous activity.
        Intent intent = getIntent();
        String listKey = intent.getStringExtra(GroceryList.LIST_KEY_STRING);
        String listTitle = intent.getStringExtra(GroceryList.TITLE_STRING);

        // Set this activity's title.
        setTitle(listTitle);

        // Save the add button for animations later
        addNewButton = (ImageButton) findViewById(R.id.add_new_object_button);

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        adapter = new GroceryRequestTableAdapter(this);
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        // Click - toggle purchased
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // When an item was clicked, toggle its 'purchased' field.
                GroceryRequest request = adapter.getRequest(position);
                db.togglePurchased(request.getKey(), request.getPurchased());
            }
        });

        // Long click - edit item name
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String createdBy = adapter.getRequest(i).getUserKey();

                // Make sure this request was created by the editing user
                if (AuthenticationManager.getInstance().getCurrentUserId().equals(createdBy)) {
                    adapter.startEditing(i);

                    View focus = getCurrentFocus();

                    if (focus != null) {
                        // Show the keyboard
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    }

                    // Hide the add new request button as long as we are editing
                    hideNewObjectButton();
                }

                return false;
            }
        });

        ObjectHandler<GroceryRequest> requestReceivedHandler = new ObjectHandler<GroceryRequest>() {
            @Override
            public void onObjectReceived(GroceryRequest request) {
                adapter.onRequestReceived(request);
            }

            @Override
            public void removeAllObjects() {
                adapter.removeAllRequests();
            }
        };

        // Create a new RequestsDB specific to this GroceryList.
        db = new RequestsModel(listKey);
        db.observeRequestsAddition(requestReceivedHandler);
    }

    /**
     * This is the function that is called when a user edits request item name
     */
    public void finishedEditing(View view) {
        String newItemName = adapter.getNewItemName();
        String requestKey = adapter.getEditingRequestKey();

        // Update the request in db.
        db.updateItemName(requestKey, newItemName);

        adapter.stopEditing();

        hideKeyboard();

        // Show the add new request button
        showNewObjectButton();
    }

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();

        if (focus != null) {
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    protected void newObjectDialog(View view) {
        this.newObjectDialog(this);
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_request_dialog);
        dialog.setTitle(context.getString(R.string.new_request));

        // Get the EditText and focus on it.
        final EditText itemNameText = (EditText) dialog.findViewById(R.id.itemNameText);
        itemNameText.requestFocus();

        ImageButton confirmButton = (ImageButton) dialog.findViewById(R.id.confirm);

        // If button is clicked, close the custom dialog
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                String itemName = itemNameText.getText().toString();

                // Get userKey from Auth
                String userKey = AuthenticationManager.getInstance().getCurrentUserId();

                // Add the new request to the database.
                GroceryRequest newRequest = new GroceryRequest(itemName, userKey);
                db.addNewRequest(newRequest);
            }
        });

        dialog.show();
    }
}
