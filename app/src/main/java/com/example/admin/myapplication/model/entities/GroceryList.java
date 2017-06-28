package com.example.admin.myapplication.model.entities;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryList implements Comparable<GroceryList> {
    private String key;
    private String groupKey;
    private String title;
    private Boolean archived = false;

    public String getKey() {
        return key;
    }
    public String getGroupKey() { return groupKey; }
    public String getTitle() {
        return title;
    }
    public Boolean getIsArchived() {
        return archived;
    }

    /**
     * This constructor is only called when a user creates a new GroceryList.
     * It will always be relevant at first.
     */
    public GroceryList(String key, String groupKey, String title) {
        this(key, groupKey, title, true);
    }

    public GroceryList(String key, String groupKey, String title , Boolean archived) {
        this.key = key;
        this.groupKey = groupKey;
        this.title = title;
        this.archived = archived;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupKey", groupKey);
        result.put("title", title);
        result.put("archived", archived);

        return result;
    }

    @Override
    public int compareTo(GroceryList list) {
        return title.compareTo(list.getTitle());
    }
}
