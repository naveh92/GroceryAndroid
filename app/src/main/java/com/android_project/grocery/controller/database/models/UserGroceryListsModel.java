package com.android_project.grocery.controller.database.models;

import com.android_project.grocery.controller.authentication.AuthenticationManager;
import com.android_project.grocery.controller.handlers.ObjectReceivedHandler;
import com.android_project.grocery.model.entities.GroceryList;
import com.android_project.grocery.model.entities.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gun2f on 6/18/2017.
 *
 * This Model starts the list retrieval process:
 *  It fetches all groups in which the user is participating.
 *  For every received group, it fetches all the relevant lists associated with it.
 */
public class UserGroceryListsModel extends AbstractModel {
    private UserGroupsModel groupsDBModel;
    private final List<GroceryList> lists = new ArrayList<>();
    private List<GroceryListsByGroupModel> listsDbs = new ArrayList<>();
    private static UserGroceryListsModel instance;

    private UserGroceryListsModel(String userKey) {
        groupsDBModel = new UserGroupsModel(userKey);
    }

    /**
     * The instance we are creating is specific for the current userKey.
     */
    public static UserGroceryListsModel getInstance() {
        if (instance == null) {
            instance = new UserGroceryListsModel(AuthenticationManager.getInstance().getCurrentUserId());
        }
        return instance;
    }

    /**
     * When logging-out, we should discard the current instance,
     * because we may login to a different user later.
     */
    public static void destroyInstance() {
        instance = null;
    }

    public void observeLists(final ObjectReceivedHandler<GroceryList> listAdded, final ObjectReceivedHandler<GroceryList> listDeleted) {
        // ----------------------------------
        //  Handlers for added/removed lists
        // ----------------------------------
        // Add
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
        // Remove
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

        // -----------------------------------
        //  Handlers for added/removed groups
        // -----------------------------------
        // Add
        ObjectReceivedHandler<Group> groupAdded = new ObjectReceivedHandler<Group>() {
            @Override
            public void onObjectReceived(Group addedGroup) {
                // Make sure we didn't already add this group's grocery lists (Could happen when UserGroupsDB resets).
                if (!containsListDb(addedGroup.getKey())) {
                    // Create a db that manages the added group's lists
                    GroceryListsByGroupModel groupListsDb = new GroceryListsByGroupModel(addedGroup.getKey());
                    listsDbs.add(groupListsDb);

                    // When the new db observes a new list, add it to our array of lists.
                    groupListsDb.observeLists(privateListAdded ,privateListDeleted);
                }
            }
        };

        // Observe the groups in which the user is in.
        // For each incoming group, we will fetch all lists associated with it.
        groupsDBModel.observeUserGroupsAddition(groupAdded);
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
        for (GroceryListsByGroupModel db : listsDbs) {
            if (db.getGroupKey().equals(groupKey)) {
                return true;
            }
        }

        return false;
    }

    public void removeGroupLists(String groupKey, ObjectReceivedHandler<GroceryList> listRemovedHandler) {
        List<GroceryList> listsToRemove = new ArrayList<>();

        synchronized (lists) {
            for (GroceryList list : lists) {
                if (list.getGroupKey().equals(groupKey)) {
                    listsToRemove.add(list);
                }
            }

            for (GroceryList list : listsToRemove) {
                // Remove all the lists we found
                lists.remove(list);

                // Notify the callback for every list removed.
                listRemovedHandler.onObjectReceived(list);
            }
        }
    }


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

    // TODO: Remove group observer when leaving a group (Could be called from removeGroupLists maybe?)
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
}