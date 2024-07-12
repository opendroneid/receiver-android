package org.opendroneid.android.app.network.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class UrlManager {
    private static final String PREF_NAME = "UrlPrefs";
    private static final String KEY_URL = "url";
    private static final String KEY_URL_DETECTION = "detection_url";

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

    ///

    public static void saveDetectionUrl(Context context, String url) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_URL_DETECTION, url);
        editor.apply();
    }
    public static String getDetectionUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_URL_DETECTION, null);
    }
    public static void deleteDetectionUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_URL_DETECTION);
        editor.apply();
    }
}