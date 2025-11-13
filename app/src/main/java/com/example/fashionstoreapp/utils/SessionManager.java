package com.example.fashionstoreapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fashionstoreapp.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "FashionStoreSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    // Create login session
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user data
    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    // Update user data
    public void updateUser(User user) {
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    // Logout user
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // Get user ID
    public String getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    // Get user email
    public String getUserEmail() {
        User user = getUser();
        return user != null ? user.getEmail() : null;
    }

    // Get user name
    public String getUserName() {
        User user = getUser();
        return user != null ? user.getDisplayName() : null;
    }
}
