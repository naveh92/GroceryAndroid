package com.example.admin.myapplication.controller.database.models;

import com.example.admin.myapplication.controller.database.local.GroupsTable;
import com.example.admin.myapplication.controller.database.remote.GroupsDB;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.Group;

/**
 * Created by gun2f on 6/19/2017.
 *
 * This Model tries to get the Group from local DB first,
 * and if it doesn't exist, fetches it from Remote DB and adds it to local.
 */
public class GroupsModel extends AbstractModel {
    private static GroupsModel instance;
    private static GroupsTable table;
    private GroupsModel() {
        table = new GroupsTable();
    }

    public static GroupsModel getInstance() {
        if (instance == null) {
            instance = new GroupsModel();
        }

        return instance;
    }

    public String addNewGroup(Group group) {
        // Local
        addNewGroupToLocal(group);

        // Remote
        String newGroupKey = GroupsDB.getInstance().addNewGroup(group);
        return newGroupKey;
    }

    public void deleteGroup(String groupKey) {
        // Local
        table.deleteGroup(groupKey);
//      TODO: LastUpdatedTable? We don't really use this..

        // Remote
        GroupsDB.getInstance().deleteGroup(groupKey);
    }

    public void findGroupByKey(String groupKey, final ObjectReceivedHandler<Group> handler) {
        // Try to get the group from the local DB.
        Group group = table.getGroupByKey(groupKey);

        if (group != null) {
            handler.onObjectReceived(group);
        }
        else {
            final ObjectReceivedHandler<Group> remoteUserReceivedHandler = new ObjectReceivedHandler<Group>() {
                @Override
                public void onObjectReceived(Group group) {
                    if (group != null) {
                        // Save the remote group to the local db.
                        addNewGroupToLocal(group);
                    }

                    // Pass it on.
                    handler.onObjectReceived(group);
                }
            };

            // If localDB failed, try to fetch from remote.
            GroupsDB.getInstance().findGroupByKey(groupKey, remoteUserReceivedHandler);
        }
    }

    private void addNewGroupToLocal(Group group) {
        table.addNewGroup(group);
//      TODO: LastUpdatedTable? We don't really use this..
    }
}
