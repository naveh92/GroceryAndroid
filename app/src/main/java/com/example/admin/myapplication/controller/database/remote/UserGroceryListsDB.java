package com.example.admin.myapplication.controller.database.remote;

import com.example.admin.myapplication.model.entities.GroceryList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 07/04/2017.
 */
public class UserGroceryListsDB {
    private List<GroceryList> lists = new ArrayList<>();
    private UserGroupsDB groupsDB;
    private  List<GroceryListsByGroupDB> listsDbs = new ArrayList<>();

//    var whenListAddedAtIndex: ((_: Int) -> Void)?
//    var whenListDeletedAtIndex: ((_: Int?) -> Void)?

    public UserGroceryListsDB(String userKey) {
        groupsDB = new UserGroupsDB(userKey);
    }

//    func observeLists(whenListAddedAtIndex: @escaping (_: Int) -> Void, whenListDeletedAtIndex: @escaping(_: Int?) -> Void) {
//        self.whenListAddedAtIndex = whenListAddedAtIndex
//        self.whenListDeletedAtIndex = whenListDeletedAtIndex
//        groupsDb!.observeUserGroupsAddition(whenGroupAdded: groupAdded)
//        groupsDb!.observeUserGroupsDeletion(whenGroupDeleted: groupDeleted)
//    }
//
//    private func groupAdded(groupIndex: Int) {
//        let group = groupsDb!.getGroup(row: groupIndex)
//
//        // Make sure we didn't already add this group's grocery lists (Could happen when UserGroupsDB resets).
//        if (listsDb.index(where: {$0.groupKey == group!.key}) == nil) {
//            // Create a db that manages the added group's lists
//            let db = GroceryListsByGroupDB(groupKey: group!.key)
//            listsDb.append(db)
//
//            // When the new db observes a new list, add it to our array of lists.
//            db.observeListsAddition(whenAdded: listAdded)
//            db.observeListsDeletion(whenDeleted: listDeleted)
//        }
//    }
//
//    private func listAdded(addedList: GroceryList) {
//        lists.append(addedList)
//        whenListAddedAtIndex!(lists.count - 1)
//    }
//
//    private func listDeleted(deletedList: GroceryList) {
//        let deletedListIndex = lists.index(where: { $0.id == deletedList.id })!
//                lists.remove(at: deletedListIndex)
//        whenListDeletedAtIndex!(deletedListIndex)
//    }
//
//    private func groupDeleted(_: Int, deletedGroup: Group) {
//        removeGroupObserver(groupKey: deletedGroup.key)
//        removeGroupLists(groupKey: deletedGroup.key)
//        whenListDeletedAtIndex!(nil)
//    }
//
//    private func removeGroupObserver(groupKey: NSString) {
//        guard let dbIndex = listsDb.index(where: { $0.groupKey == groupKey }) else { return }
//
//        // Remove the observers and remove the db of the deleted group.
//        listsDb[dbIndex].removeObservers()
//        listsDb.remove(at: dbIndex)
//    }
//
//    private func removeGroupLists(groupKey: NSString) {
//        lists = lists.filter({ $0.groupKey != groupKey })
//    }
//
//    func removeObservers() {
//        groupsDb!.removeObservers()
//        listsDb.forEach({ $0.removeObservers() })
//    }
//
//    func getListsCount() -> Int {
//        return lists.count
//    }
//
//    func doesUserHaveGroup() -> Bool {
//        return groupsDb!.getGroupsCount() > 0
//    }
//
//    func getGroceryList(row: Int) -> GroceryList? {
//        if (row < getListsCount()) {
//            return lists[row]
//        }
//
//        return nil
//    }
}