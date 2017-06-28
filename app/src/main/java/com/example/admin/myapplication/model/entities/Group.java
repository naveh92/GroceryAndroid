package com.example.admin.myapplication.model.entities;

/**
 * Created by admin on 04/04/2017.
 */

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 04/04/2017.
 */
public class Group implements Comparable<Group> {
    private String key;
    private String title;
    private Boolean relevant;

    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }
    public Boolean isRelevant() {
        return relevant;
    }

    /**
     * This constructor is only for when a user creates a new group.
     * It should be relevant.
     */
    public Group(String key, String title) {
        this(key, title, true);
    }

    public Group(String key, String title, Boolean relevant) {
        this.key = key;
        this.title = title;
        this.relevant = relevant;
    }

    @Override
    public String toString() {
        return title;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("relevant", relevant);

        return result;
    }

    @Override
    public int compareTo(Group group) {
        return title.compareTo(group.getTitle());
    }
}