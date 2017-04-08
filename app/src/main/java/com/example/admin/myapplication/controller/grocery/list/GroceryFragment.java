package com.example.admin.myapplication.controller.grocery.list;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.TableViewFragment;
import com.example.admin.myapplication.controller.authentication.AuthenticationManager;
import com.example.admin.myapplication.controller.database.remote.GroceryListsByGroupDB;
import com.example.admin.myapplication.controller.database.remote.ListsDB;
import com.example.admin.myapplication.controller.database.remote.UserGroceryListsDB;
import com.example.admin.myapplication.controller.database.remote.UserGroupsDB;
import com.example.admin.myapplication.controller.grocery.request.GroceryRequestsTableActivity;
import com.example.admin.myapplication.controller.grocery.request.GroupComboBoxAdapter;
import com.example.admin.myapplication.controller.handlers.ListReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryFragment extends TableViewFragment {
    private static UserGroceryListsDB db;
    private GroceryListTableAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        if (db == null) {
            db = new UserGroceryListsDB(AuthenticationManager.getInstance().getCurrentUserId());
        }

        // Save the add button for animations later
        addNewButton = (ImageButton) view.findViewById(R.id.add_new_object_button);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        adapter = new GroceryListTableAdapter(getActivity(), db);
        gridview.setAdapter(adapter);

        // Register the animations when gridview is touched.
        super.createHideViewsWhenScroll(gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Open an activity for the list that was clicked. need to show all requests in it.
                Intent intent = new Intent(getActivity(), GroceryRequestsTableActivity.class);

                GroceryList list = db.getGroceryList(position);
                intent.putExtra("listKey", list.getKey()); // Add the listKey for the next activity.
                intent.putExtra("listTitle", list.getTitle()); // Add the listTitle for the next activity.

                startActivity(intent);
            }
        });

        fetchLists();

        return view;
    }

    @Override
    public void newObjectDialog(Context context) {
        // Open a dialog.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_list_dialog);
        dialog.setTitle("New Grocery List");

        // Get the EditText and focus on it.
        final EditText listTitleText = (EditText) dialog.findViewById(R.id.listTitleText);
        listTitleText.requestFocus();

        final Spinner groupComboBox = (Spinner) dialog.findViewById(R.id.spinner);
        GroupComboBoxAdapter comboBoxAdapter = new GroupComboBoxAdapter(context,
                                                                android.R.layout.simple_spinner_item,
                                                                db.getAllGroups());
        groupComboBox.setAdapter(comboBoxAdapter);

        ImageButton confirmButton = (ImageButton) dialog.findViewById(R.id.confirm);

        // If button is clicked, close the custom dialog
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // Get the user input.
                String listTitle = listTitleText.getText().toString();
                String groupKey = ((Group)groupComboBox.getSelectedItem()).getKey();

                // Add the new list to the database.
                GroceryList newList = new GroceryList("", groupKey, listTitle);
                ListsDB.getInstance().addNewList(newList);

                fetchLists();
            }
        });

        dialog.show();
    }

    @Override
    public void refresh() {
        fetchLists();
    }

    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteList(int position) {
        GroceryList list = db.getGroceryList(position);

        if (list != null) {
            String listKey = list.getKey();
            ListsDB.getInstance().deleteList(listKey);
        }

        notifyDataSetChanged();
    }

    private void fetchLists() {
        ListReceivedHandler listReceivedHandler = new ListReceivedHandler() {
            @Override
            public void onListReceived(GroceryList list) {
                notifyDataSetChanged();
            }

            @Override
            public void removeAllLists() {
                // TODO: Delete this? Check if it is even being called..
                notifyDataSetChanged();
            }
        };

        ListReceivedHandler groupListDeletedHandler = new ListReceivedHandler() {
            @Override
            public void onListReceived(GroceryList list) {
                notifyDataSetChanged();
            }

            @Override
            public void removeAllLists() {}
        };

        if (db == null) {
            db = new UserGroceryListsDB(AuthenticationManager.getInstance().getCurrentUserId());
        }

        db.observeLists(listReceivedHandler, groupListDeletedHandler);
    }
}