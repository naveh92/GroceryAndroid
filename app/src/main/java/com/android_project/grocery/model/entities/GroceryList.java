package com.android_project.grocery.model.entities;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class GroceryList implements Comparable<GroceryList> {
    public static final String GROUP_KEY_STRING = "groupKey";
    public static final String TITLE_STRING = "title";
    public static final String RELEVANT_STRING = "relevant";
    public static final String LIST_KEY_STRING = "listKey";

    private String key;
    private String groupKey;
    private String title;
    private Boolean relevant;

    public String getKey() {
        return key;
    }
    public String getGroupKey() { return groupKey; }
    public String getTitle() {
        return title;
    }
    public Boolean isRelevant() {
        return relevant;
    }

    /**
     * This setter is for when we add a new list and the key is generated in remote DB.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * This constructor is only called when a user creates a new GroceryList.
     * It will always be relevant at first.
     */
    public GroceryList(String key, String groupKey, String title) {
        this(key, groupKey, title, true);
    }

    public GroceryList(String key, String groupKey, String title , Boolean relevant) {
        this.key = key;
        this.groupKey = groupKey;
        this.title = title;
        this.relevant = relevant;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(GROUP_KEY_STRING, groupKey);
        result.put(TITLE_STRING, title);
        result.put(RELEVANT_STRING, relevant);

        return result;
    }

    @Override
    public int compareTo(GroceryList list) {
        return title.compareTo(list.getTitle());
    }
}
