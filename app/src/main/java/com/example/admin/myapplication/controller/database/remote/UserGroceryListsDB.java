package com.example.admin.myapplication.controller.database.remote;

import android.content.Context;

import com.example.admin.myapplication.controller.database.models.UserGroupsModel;
import com.example.admin.myapplication.controller.handlers.ObjectReceivedHandler;
import com.example.admin.myapplication.model.entities.GroceryList;
import com.example.admin.myapplication.model.entities.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 07/04/2017.
 */
public class UserGroceryListsDB {
    private final List<GroceryList> lists = new ArrayList<>();
    private UserGroupsModel groupsDBModel;
    private List<GroceryListsByGroupDB> listsDbs = new ArrayList<>();

    public UserGroceryListsDB(String userKey) {
        groupsDBModel = new UserGroupsModel(userKey);
    }

    public void observeLists(final ObjectReceivedHandler<GroceryList> listAdded, final ObjectReceivedHandler<GroceryList> listDeleted) {
        final ObjectReceivedHandler<GroceryList> privateListAdded = new ObjectReceivedHandler<GroceryList>() {
            @Override
            public void onObjectReceived(GroceryList addedList) {
                synchronized (lists) {
                    // Make sure the list doesn't already exist. (Just in case..)
                    // And make sure the list is not archived
                    if (addedList.isRelevant() && !containsList(addedList)) {
                        lists.add(addedList);
                        Collections.sort(lists);
                    }
                }

                // After adding to our private collection, notify the original callback
                listAdded.onObjectReceived(addedList);
            }
        };

        final ObjectReceivedHandler<GroceryList> privateListDeleted = new ObjectReceivedHandler<GroceryList>() {
            @Override
            public void onObjectReceived(GroceryList deletedList) {
                removeListByKey(deletedList.getKey());

                // After removing from our private collection, notify the original callback
                listDeleted.onObjectReceived(deletedList);
            }

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
                        Collections.sort(lists);
                    }
                }
            }
        };

        ObjectReceivedHandler<Group> groupAdded = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group addedGroup) {
                // Make sure we didn't already add this group's grocery lists (Could happen when UserGroupsDB resets).
                if (!containsListDb(addedGroup.getKey())) {
                    // Create a db that manages the added group's lists
                    GroceryListsByGroupDB groupListsDb = new GroceryListsByGroupDB(addedGroup.getKey());
                    listsDbs.add(groupListsDb);

                    // When the new db observes a new list, add it to our array of lists.
                    groupListsDb.observeLists(privateListAdded ,privateListDeleted);
                }
            }
        };

        // TODO: Need this? because we registered to listAdded and listDeleted up
        ObjectReceivedHandler<Group> groupDeleted = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group deletedGroup) {
                // TODO: ?
//        removeGroupObserver(groupKey: deletedGroup.key)

                removeGroupLists(deletedGroup.getKey(), privateListDeleted);
            }
        };

        groupsDBModel.observeUserGroupsAddition(groupAdded);
        groupsDBModel.observeUserGroupsDeletion(groupDeleted);
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

    private void removeGroupLists(String groupKey, ObjectReceivedHandler<GroceryList> listRemovedHandler) {
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
                listRemovedHandler.onObjectReceived(list);
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

    public Boolean doesUserHaveGroup() {
        return groupsDBModel.getGroupsCount() > 0;
    }

    public GroceryList getGroceryList(int position) {
        synchronized (lists) {
            if (position < getListsCount()) {
                return lists.get(position);
            }
        }

        return null;
    }

    public List<Group> getAllGroups() {
        return groupsDBModel.getAllGroups();
    }
}