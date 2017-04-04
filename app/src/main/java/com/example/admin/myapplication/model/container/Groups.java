package com.example.admin.myapplication.model.container;

import com.example.admin.myapplication.model.entities.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 04/04/2017.
 */
public class Groups {
    private static List<Group> groups = new ArrayList<>();

    public static int size() {
        return groups.size();
    }

    public static Group get(int position) {
        return groups.get(position);
    }

    public static void add(Group group) {
        groups.add(group);
    }

    public static void clear() {
        groups.clear();
    }

    public static String title(String groupKey) {
        // TODO: Java8? groups.stream().filter(group -> group.getKey().equals(groupKey)).collect(Collectors.toList())???
        for (Group group : groups) {
            if (group.getKey().equals(groupKey)) {
                return group.getTitle();
            }
        }

        // TODO: strings.xml
        return "N/A";
    }
}
