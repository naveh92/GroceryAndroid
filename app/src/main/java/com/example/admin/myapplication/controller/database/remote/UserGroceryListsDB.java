package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.controller.handlers.GroupReceivedHandler;
import com.example.admin.myapplication.controller.handlers.ListReceivedHandler;
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

    public void observeLists(final ListReceivedHandler listAdded, final ListReceivedHandler listDeleted) {
        final ListReceivedHandler privateListAdded = new ListReceivedHandler() {
            @Override
            public void onListReceived(GroceryList addedList) {
                synchronized (lists) {
                    // Make sure the list doesn't already exist. (Just in case..)
                    if (!containsList(addedList)) {
                        lists.add(addedList);
                    }
                }

                // After adding to our private collection, notify the original callback
                listAdded.onListReceived(addedList);
            }

            @Override
            public void removeAllLists() {}
        };

        final ListReceivedHandler privateListDeleted = new ListReceivedHandler() {
            @Override
            public void onListReceived(GroceryList deletedList) {
                removeListByKey(deletedList.getKey());

                // After removing from our private collection, notify the original callback
                listDeleted.onListReceived(deletedList);
            }

            @Override
            public void removeAllLists() {}

            private void removeListByKey(String listKey) {
                GroceryList listToDelete = null;

                synchronized (lists) {
                    // Find the list to delete
                    for (GroceryList list : lists) {
                        if (list.getKey().equals(listKey)) {
                            listToDelete = list;
                        }
                    }

                    if (listToDelete != null) {
                        // Delete the list
                        lists.remove(listToDelete);
                    }
                }
            }
        };

        GroupReceivedHandler groupAdded = new GroupReceivedHandler() {
            @Override
            public void onGroupReceived(Group addedGroup) {
                // Make sure we didn't already add this group's grocery lists (Could happen when UserGroupsDB resets).
                if (!containsListDb(addedGroup.getKey())) {
                    // Create a db that manages the added group's lists
                    GroceryListsByGroupDB groupListsDb = new GroceryListsByGroupDB(addedGroup.getKey());
                    listsDbs.add(groupListsDb);

                    // When the new db observes a new list, add it to our array of lists.
                    groupListsDb.observeLists(privateListAdded ,privateListDeleted);
                }
            }

            @Override
            public void removeAllGroups() {}
        };

        // TODO: Need this? because we registered to listAdded and listDeleted up
        GroupReceivedHandler groupDeleted = new GroupReceivedHandler() {
            @Override
            public void onGroupReceived(Group deletedGroup) {
                // TODO: ?
//        removeGroupObserver(groupKey: deletedGroup.key)

                removeGroupLists(deletedGroup.getKey(), privateListDeleted);
            }

            @Override
            public void removeAllGroups() {}
        };

        groupsDB.observeUserGroupsAddition(groupAdded);
        groupsDB.observeUserGroupsDeletion(groupDeleted);
    }

    private Boolean containsList(GroceryList list) {
        synchronized (lists) {
            for (GroceryList l : lists) {
                if (l.getKey().equals(list.getKey())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Boolean containsListDb(String groupKey) {
        for (GroceryListsByGroupDB db : listsDbs) {
            if (db.getGroupKey().equals(groupKey)) {
                return true;
            }
        }

        return false;
    }

    private void removeGroupLists(String groupKey, ListReceivedHandler listRemovedHandler) {
        List<GroceryList> listsToRemove = new ArrayList<>();

        synchronized (lists) {
            for (GroceryList list : lists) {
                if (list.getGroupKey().equals(groupKey)) {
                    listsToRemove.add(list);
                }
            }
            // TODO: Close the synchronized here?

            for (GroceryList list : listsToRemove) {
                // Remove all the lists we found
                lists.remove(listsToRemove);

                // Notify the callback for every list removed.
                listRemovedHandler.onListReceived(list);
            }
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
        int count;

        synchronized (lists) {
            count = lists.size();
        }

        return count;
    }

    // TODO:
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

    public void removeList(GroceryList list) {
        lists.remove(list);
    }
}