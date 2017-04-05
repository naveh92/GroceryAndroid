package com.example.admin.myapplication.controller.grocery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.TableView;
import com.example.admin.myapplication.controller.database.remote.RequestsDB;
import com.example.admin.myapplication.model.entities.GroceryRequest;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequestsTableActivity extends Activity implements TableView {
    private RequestsDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_view);

        // Retrieve the listKey from the previous activity.
        Intent intent = getIntent();
        String listKey = intent.getStringExtra("listKey");
        String listTitle = intent.getStringExtra("listTitle");

        // Set this activity's title.
        setTitle(listTitle);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        final GroceryRequestTableAdapter adapter = new GroceryRequestTableAdapter(this);
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // When an item was clicked, toggle its 'purchased' field.
                GroceryRequest request = adapter.getRequest(position);
                db.togglePurchased(request.getKey(), request.getPurchased());
            }
        });

        ObjectReceivedHandler requestReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object request) {
                adapter.onRequestReceived((GroceryRequest) request);
            }

            @Override
            public void removeAllObjects() {
                adapter.removeAllRequests();
            }
        };

        // Create a new RequestsDB specific to this GroceryList.
        db = new RequestsDB(listKey);
        db.observeRequestsAddition(requestReceivedHandler);
    }

    protected void add(View view) {
        this.add();
    }

    @Override
    public void add() {
        // Open a dialog.
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.new_request_dialog);
        dialog.setTitle("New Request");

        ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.confirm);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // TODO: db.addNewRequest();
            }
        });

        dialog.show();
    }
}
