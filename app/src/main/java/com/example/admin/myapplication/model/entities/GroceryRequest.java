package com.example.admin.myapplication.model.entities;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 05/04/2017.
 */
public class GroceryRequest {
    private String key;
    private String userKey;
    private String itemName;
    private Boolean purchased;

    public String getKey() {
        return key;
    }
    public String getUserKey() { return userKey; }
    public String getItemName() {
        return itemName;
    }
    public Boolean getPurchased() { return purchased; }

    public GroceryRequest(String itemName, String userKey) {
        this.itemName = itemName;
        this.userKey = userKey;
        this.purchased = false;
    }

    public GroceryRequest(String key, String userKey, String itemName, Boolean purchased) {
        this.key = key;
        this.userKey = userKey;
        this.itemName = itemName;
        this.purchased = purchased;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("itemName", itemName);
        result.put("purchased", purchased.toString());
        result.put("userId", userKey);

        return result;
    }
}