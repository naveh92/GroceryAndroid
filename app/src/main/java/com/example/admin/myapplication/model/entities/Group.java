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
public class Group {
    private String key;
    private String title;

    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }

    public Group(String key, String title) {
        this.key = key;
        this.title = title;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("title", title);

        return result;
    }
}