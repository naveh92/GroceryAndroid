package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.controller.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 07/04/2017.
 */
public class UserGroceryListsDB {
    private List<GroceryList> lists = new ArrayList<>();
    private UserGroupsDB groupsDB;
    private List<GroceryListsByGroupDB> listsDbs = new ArrayList<>();

    public UserGroceryListsDB(String userKey) {
        groupsDB = new UserGroupsDB(userKey);
    }

    public void observeLists(final ObjectReceivedHandler listAdded, final ObjectReceivedHandler listDeleted) {
        final ObjectReceivedHandler privateListAdded = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                GroceryList addedList = (GroceryList) obj;

                synchronized (lists) {
                    // Make sure the list doesn't already exist. (Just in case..)
                    if (!lists.contains(addedList)) {
                        lists.add(addedList);
                    }
                }

                listAdded.onObjectReceived(addedList);
            }

            @Override
            public void removeAllObjects() {}
        };

        final ObjectReceivedHandler privateListDeleted = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                GroceryList deletedList = (GroceryList) obj;

                synchronized (lists) {
                    lists.remove(deletedList);
                }

                listDeleted.onObjectReceived(deletedList);
            }

            @Override
            public void removeAllObjects() {}
        };

        ObjectReceivedHandler groupAdded = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                Group group = (Group) obj;

                // Make sure we didn't already add this group's grocery lists (Could happen when UserGroupsDB resets).
                if (!containsListDb(group.getKey())) {
                    // Create a db that manages the added group's lists
                    GroceryListsByGroupDB groupListsDb = new GroceryListsByGroupDB(group.getKey());
                    listsDbs.add(groupListsDb);

                    // When the new db observes a new list, add it to our array of lists.
                    groupListsDb.observeLists(privateListAdded ,privateListDeleted);
                }
            }

            @Override
            public void removeAllObjects() {}
        };

        // TODO: Need this? because we registered to listAdded and listDeleted up
        ObjectReceivedHandler groupDeleted = new ObjectReceivedHandler() {
            @Override
            public void onObjectReceived(Object obj) {
                Group deletedGroup = (Group) obj;
                // TODO: ?
//        removeGroupObserver(groupKey: deletedGroup.key)

                removeGroupLists(deletedGroup.getKey(), privateListDeleted);
            }

            @Override
            public void removeAllObjects() {}
        };

        groupsDB.observeUserGroupsAddition(groupAdded);
        groupsDB.observeUserGroupsDeletion(groupDeleted);
    }

    private boolean containsListDb(String groupKey) {
        for (GroceryListsByGroupDB db : listsDbs) {
            if (db.getGroupKey().equals(groupKey)) {
                return true;
            }
        }

        return false;
    }

    private void removeGroupLists(String groupKey, ObjectReceivedHandler listRemovedHandler) {
        List<GroceryList> listsToRemove = new ArrayList<>();

        synchronized (lists) {
            for (GroceryList list : lists) {
                if (list.getGroupKey().equals(groupKey)) {
                    listsToRemove.add(list);

                    // Notify the callback for every list removed.
                    listRemovedHandler.onObjectReceived(list);
                }
            }

            // Remove all the lists we found
            lists.removeAll(listsToRemove);
        }
    }

    // TODO: ?
//    private func removeGroupObserver(groupKey: NSString) {
//        guard let dbIndex = listsDb.index(where: { $0.groupKey == groupKey }) else { return }
//
//        // Remove the observers and remove the db of the deleted group.
//        listsDb[dbIndex].removeObservers()
//        listsDb.remove(at: dbIndex)
//    }
//
//
//    func removeObservers() {
//        groupsDb!.removeObservers()
//        listsDb.forEach({ $0.removeObservers() })
//    }
//

    public int getListsCount() {
        return lists.size();
    }

    public Boolean doesUserHaveGroup() {
        return UserGroupsDB.getGroupsCount() > 0;
    }

    public GroceryList getGroceryList(int position) {
        synchronized (lists) {
            if (position < getListsCount()) {
                return lists.get(position);
            }
        }

        return null;
    }
}