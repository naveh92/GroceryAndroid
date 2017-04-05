package com.example.admin.myapplication.controller.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.TableView;
import com.example.admin.myapplication.controller.database.remote.RemoteDatabaseManager;
import com.example.admin.myapplication.model.entities.GroceryList;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryFragment extends Fragment implements TableView {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        final GroceryListTableAdapter adapter = new GroceryListTableAdapter(getActivity());
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Open an activity for the list that was clicked. need to show all requests in it.
                Intent intent = new Intent(getActivity(), GroceryRequestsTableActivity.class);

                GroceryList list = adapter.getList(position);
                intent.putExtra("listKey", list.getKey()); // Add the listKey for the next activity.
                intent.putExtra("listTitle", list.getTitle()); // Add the listTitle for the next activity.

                startActivity(intent);
            }
        });

        ObjectReceivedHandler listReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object list) {
                adapter.onListReceived((GroceryList) list);
            }

            @Override
            public void removeAllObjects() {
                adapter.removeAllLists();
            }
        };

        RemoteDatabaseManager.getInstance().observeListsAddition(listReceivedHandler);

        return view;
    }

    @Override
    public void add() {

    }
}