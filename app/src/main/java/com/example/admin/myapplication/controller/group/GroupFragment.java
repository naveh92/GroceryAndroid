package com.example.admin.myapplication.controller.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by admin on 04/04/2017.
 */
public class GroupFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.table_view, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        final GroupTableAdapter adapter = new GroupTableAdapter(getActivity());
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO: Open activity for the group that was clicked. need to show all members in it.
            }
        });

        ObjectReceivedHandler groupReceivedHandler = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object group) {
                adapter.onGroupReceived((Group) group);
            }

            @Override
            public void removeAllObjects() {}
        };

        GroupsDB.getInstance().observeGroupsAddition(groupReceivedHandler);

        return view;
    }
}