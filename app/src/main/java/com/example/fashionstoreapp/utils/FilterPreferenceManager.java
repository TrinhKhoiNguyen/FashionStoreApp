package com.example.fashionstoreapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fashionstoreapp.models.FilterCriteria;
import com.example.fashionstoreapp.models.SortOption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to persist filter and sort preferences using SharedPreferences
 * Allows users to restore their last filter/sort state when reopening the app
 */
public class FilterPreferenceManager {

    private static final String PREF_NAME = "FilterPreferences";
    private static final String KEY_MIN_PRICE = "minPrice";
    private static final String KEY_MAX_PRICE = "maxPrice";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_SIZES = "sizes";
    private static final String KEY_IN_STOCK_ONLY = "inStockOnly";
    private static final String KEY_MIN_RATING = "minRating";
    private static final String KEY_SORT_OPTION = "sortOption";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FilterPreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Save filter criteria to SharedPreferences
     */
    public void saveFilterCriteria(FilterCriteria criteria) {
        SharedPreferences.Editor editor = prefs.edit();

        if (criteria.getMinPrice() != null) {
            editor.putFloat(KEY_MIN_PRICE, criteria.getMinPrice().floatValue());
        } else {
            editor.remove(KEY_MIN_PRICE);
        }

        if (criteria.getMaxPrice() != null) {
            editor.putFloat(KEY_MAX_PRICE, criteria.getMaxPrice().floatValue());
        } else {
            editor.remove(KEY_MAX_PRICE);
        }

        // Save lists as JSON
        String categoriesJson = gson.toJson(criteria.getCategories());
        editor.putString(KEY_CATEGORIES, categoriesJson);

        String sizesJson = gson.toJson(criteria.getSizes());
        editor.putString(KEY_SIZES, sizesJson);

        editor.putBoolean(KEY_IN_STOCK_ONLY, criteria.isInStockOnly());
        editor.putFloat(KEY_MIN_RATING, criteria.getMinRating());
        editor.putString(KEY_SORT_OPTION, criteria.getSortBy());

        editor.apply();
    }

    /**
     * Load filter criteria from SharedPreferences
     */
    public FilterCriteria loadFilterCriteria() {
        FilterCriteria criteria = new FilterCriteria();

        if (prefs.contains(KEY_MIN_PRICE)) {
            criteria.setMinPrice((double) prefs.getFloat(KEY_MIN_PRICE, 0f));
        }

        if (prefs.contains(KEY_MAX_PRICE)) {
            criteria.setMaxPrice((double) prefs.getFloat(KEY_MAX_PRICE, Float.MAX_VALUE));
        }

        // Load lists from JSON
        String categoriesJson = prefs.getString(KEY_CATEGORIES, "[]");
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> categories = gson.fromJson(categoriesJson, listType);
        criteria.setCategories(categories);

        String sizesJson = prefs.getString(KEY_SIZES, "[]");
        List<String> sizes = gson.fromJson(sizesJson, listType);
        criteria.setSizes(sizes);

        criteria.setInStockOnly(prefs.getBoolean(KEY_IN_STOCK_ONLY, false));
        criteria.setMinRating(prefs.getFloat(KEY_MIN_RATING, 0f));
        criteria.setSortBy(prefs.getString(KEY_SORT_OPTION, "newest"));

        return criteria;
    }

    /**
     * Clear all saved preferences
     */
    public void clearPreferences() {
        prefs.edit().clear().apply();
    }

    /**
     * Check if there are saved preferences
     */
    public boolean hasSavedPreferences() {
        return prefs.getAll().size() > 0;
    }
}
