package org.opendroneid.android.app.network.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class UrlManager {
    private static final String PREF_NAME = "UrlPrefs";
    private static final String KEY_URL = "url";

    public static void saveUrl(Context context, String url) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_URL, url);
        editor.apply();
    }
    public static String getUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_URL, null);
    }
    public static void deleteUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_URL);
        editor.apply();
    }
}