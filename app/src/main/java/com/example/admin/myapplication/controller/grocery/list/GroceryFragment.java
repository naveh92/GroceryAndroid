package com.example.admin.myapplication.controller.grocery.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.admin.myapplication.controller.database.models.ListsModel;
import com.example.admin.myapplication.controller.database.models.UserGroceryLitsModel;
import com.example.admin.myapplication.controller.database.remote.ListsDB;
import com.example.admin.myapplication.controller.database.remote.UserGroceryListsDB;
import com.example.admin.myapplication.controller.grocery.request.GroceryRequestsTableActivity;
import com.example.admin.myapplication.controller.grocery.request.GroupComboBoxAdapter;
import com.example.admin.myapplication.controller.handlers.ObjectHandler;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryFragment extends TableViewFragment {
    // TODO: static? test this.. (If having problems with refresh)
    private static UserGroceryLitsModel db;
    private static GroceryListTableAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        if (db == null) {
            db = new UserGroceryLitsModel(AuthenticationManager.getInstance().getCurrentUserId());
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
                intent.putExtra(GroceryList.LIST_KEY_STRING, list.getKey()); // Add the listKey for the next activity.
                intent.putExtra(GroceryList.TITLE_STRING, list.getTitle()); // Add the listTitle for the next activity.

                startActivity(intent);
            }
        });

        fetchLists();

        return view;
    }

    @Override
    public void newObjectDialog(Context context) {
        if (db.doesUserHaveGroup()) {
            // Open a dialog.
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.new_list_dialog);
            dialog.setTitle(context.getString(R.string.new_grocery_list));

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
                    String groupKey = ((Group) groupComboBox.getSelectedItem()).getKey();

                    // Add the new list to the database.
                    GroceryList newList = new GroceryList("", groupKey, listTitle);
                    ListsModel.getInstance().addNewList(newList);

                    fetchLists();
                }
            });

            dialog.show();
        }
        else {
            // User doesn't have a group. He cannot create a list.
            // Show alert dialog
            new AlertDialog.Builder(context).setTitle(context.getString(R.string.sorry))
                    .setMessage(context.getString(R.string.no_group))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            // TODO: Move to group fragment?
//                            getFragmentManager()
                        }}).show();
        }
    }

    @Override
    public void refresh() {
        fetchLists();
    }

    @Override
    public void notifyDataSetChanged() {
        if (adapter != null) {
            // TODO: Which one?
            adapter.notifyDataSetInvalidated();
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteList(int position) {
        GroceryList list = db.getGroceryList(position);

        if (list != null) {
            String listKey = list.getKey();
            ListsModel.getInstance().deleteList(listKey);
        }

        notifyDataSetChanged();
    }

    private void fetchLists() {
        ObjectHandler<GroceryList> listReceivedHandler = new ObjectHandler<GroceryList>() {
            @Override
            public void onObjectReceived(GroceryList list) {
                notifyDataSetChanged();
            }

            @Override
            public void removeAllObjects() {
                // TODO: Delete this? Check if it is even being called..
                notifyDataSetChanged();
            }
        };

        ObjectReceivedHandler<GroceryList> groupListDeletedHandler = new ObjectReceivedHandler<GroceryList>() {
            @Override
            public void onObjectReceived(GroceryList list) {
                notifyDataSetChanged();
            }
        };

        // TODO: ??
//        if (db == null) {
//            db = new UserGroceryListsDB(AuthenticationManager.getInstance().getCurrentUserId());
//        }

        // TODO: ??
        if (db != null) {
            db.observeLists(getActivity(), listReceivedHandler, groupListDeletedHandler);
        }
    }
}