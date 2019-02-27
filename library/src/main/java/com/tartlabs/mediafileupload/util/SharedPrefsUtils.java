package com.tartlabs.mediafileupload.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsUtils {

    //Checks whether the preferences contains a key or not
    public static boolean contains(Context context, final String prefsName, final String key) {
        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return prefs.contains(key);
    }

    //Get String value for a particular key.
    public static String getString(Context context, final String prefsName, final String key) {

        return getString(context, prefsName, key, "");
    }

    //Get String value for a particular key.
    public static String getString(Context context, final String prefsName, final String key, final String defValue) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return prefs.getString(key, defValue);
    }

    //Get boolean value for a particular key.
    public static boolean getBoolean(Context context, final String prefsName, final String key) {

        return getBoolean(context, prefsName, key, false);
    }

    //Get boolean value for a particular key.
    public static boolean getBoolean(Context context, final String prefsName, final String key, final boolean defValue) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defValue);
    }

    //Get int value for key.
    public static int getInt(Context context, final String prefsName, final String key) {
        return getInt(context, prefsName, key, 0);
    }

    //Get int value for key.
    public static int getInt(Context context, final String prefsName, final String key, final int defValue) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return prefs.getInt(key, defValue);
    }

    //Set String value for a particular key.
    public static void set(Context context, final String prefsName, final String key, final String value) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //Set boolean value for key.
    public static void set(Context context, final String prefsName, final String key, final boolean value) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    //Set int value for key.
    public static void set(Context context, final String prefsName, final String key, final int value) {

        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    //Clear all preferences.
    public static void clearPreferences(final String prefsName, final Context context) {
        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void removeKeys(final String prefsName, final Context context, final String... keys) {
        assert (keys != null);
        final SharedPreferences prefs = context
                .getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        for (final String aKey : keys) {
            editor.remove(aKey);
        }
        editor.apply();
    }
}
