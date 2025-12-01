package com.example.fashionstoreapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fashionstoreapp.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manager for recently viewed products
 * Uses SharedPreferences to store product IDs
 */
public class RecentViewManager {
    private static final String PREF_NAME = "recent_view_prefs";
    private static final String KEY_RECENT_PRODUCT_IDS = "recent_product_ids";
    private static final int MAX_RECENT_ITEMS = 20; // Maximum recent products to store

    private static RecentViewManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private RecentViewManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized RecentViewManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecentViewManager(context);
        }
        return instance;
    }

    /**
     * Add product to recently viewed list
     * 
     * @param productId Product ID to add
     */
    public void addRecentProduct(String productId) {
        if (productId == null || productId.isEmpty()) {
            return;
        }

        List<String> recentIds = getRecentProductIds();

        // Remove if already exists (to move to front)
        recentIds.remove(productId);

        // Add to front
        recentIds.add(0, productId);

        // Limit to MAX_RECENT_ITEMS
        if (recentIds.size() > MAX_RECENT_ITEMS) {
            recentIds = recentIds.subList(0, MAX_RECENT_ITEMS);
        }

        // Save
        saveRecentProductIds(recentIds);
    }

    /**
     * Get list of recently viewed product IDs
     * 
     * @return List of product IDs (most recent first)
     */
    public List<String> getRecentProductIds() {
        String json = prefs.getString(KEY_RECENT_PRODUCT_IDS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> ids = gson.fromJson(json, type);
        return ids != null ? ids : new ArrayList<>();
    }

    /**
     * Save recent product IDs to SharedPreferences
     */
    private void saveRecentProductIds(List<String> productIds) {
        String json = gson.toJson(productIds);
        prefs.edit().putString(KEY_RECENT_PRODUCT_IDS, json).apply();
    }

    /**
     * Clear all recently viewed products
     */
    public void clearRecentProducts() {
        prefs.edit().remove(KEY_RECENT_PRODUCT_IDS).apply();
    }

    /**
     * Get count of recently viewed products
     */
    public int getRecentProductCount() {
        return getRecentProductIds().size();
    }

    /**
     * Remove a specific product from recent views
     */
    public void removeRecentProduct(String productId) {
        List<String> recentIds = getRecentProductIds();
        recentIds.remove(productId);
        saveRecentProductIds(recentIds);
    }
}
