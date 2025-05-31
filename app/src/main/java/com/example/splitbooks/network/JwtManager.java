package com.example.splitbooks.network;


import android.content.Context;
import android.content.SharedPreferences;

public class JwtManager {
    private static final String PREF_NAME = "splitbooks_prefs";
    private static final String TOKEN_KEY = "jwt_token";

    // Save JWT token
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    // Get JWT token
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    // Clear token (e.g., on logout)
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(TOKEN_KEY).apply();
    }
}