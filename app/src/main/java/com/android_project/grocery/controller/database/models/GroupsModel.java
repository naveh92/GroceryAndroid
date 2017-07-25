package com.android_project.grocery.controller.database.models;

import com.android_project.grocery.controller.database.local.GroupsTable;
import com.android_project.grocery.controller.database.remote.GroupsDB;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.Group;

/**
 * Created by gun2f on 6/19/2017.
 *
 * Group title, key, etc. never changes. Only its members do.
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

    /**
     * @returns The generated key of the new Group in the Remote DB.
     */
    public String addNewGroup(Group group) {
        // Local
        addNewGroupToLocal(group);

        // Remote
        return GroupsDB.getInstance().addNewGroup(group);
    }

    public void deleteGroup(String groupKey) {
        // Local
        table.deleteGroup(groupKey);
        // No need to update LastUpdatedTable, because we won't check the update time.

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
        // No need to update LastUpdatedTable, because we won't check the update time.
    }

    /**
     * No need to destroy - GroupsDB isn't even an AbstractRemoteDB, because it has no Listeners.
     */
    @Override
    public void destroy() {}
}
