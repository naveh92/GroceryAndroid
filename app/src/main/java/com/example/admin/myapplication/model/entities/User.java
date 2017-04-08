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
public class User implements Comparable<User> {
    private String key;
    private String facebookId;
    private String name;

    public String getKey() {
        return key;
    }
    public String getFacebookId() {
        return facebookId;
    }
    public String getName() {
        return name;
    }

    public User(String key, String facebookId, String name) {
        this.key = key;
        this.facebookId = facebookId;
        this.name = name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("facebookId", facebookId);
        result.put("name", name);

        return result;
    }

    @Override
    public int compareTo(User user) {
        return name.compareTo(user.getName());
    }
}