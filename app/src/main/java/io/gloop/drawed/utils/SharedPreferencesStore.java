package io.gloop.drawed.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import static io.gloop.drawed.SplashActivity.SHARED_PREFERENCES_FIRST_START;

/**
 * Created by Alex Untertrifaller on 06.09.17.
 */

public class SharedPreferencesStore {

    private static Context context;

    private static final String SHARED_PREFERENCES_NAME = "user";
    private static final String SHARED_PREFERENCES_USER_EMAIL = "user_email";
    private static final String SHARED_PREFERENCES_USER_PASSWORD = "user_password";

    private static final String SHARED_PREFERENCES_NIGHT_MODE = "night_mode";

    public static void setContext(Context c) {
        context = c;
    }

    public static boolean isFirstStart() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHARED_PREFERENCES_FIRST_START, true);
    }

    public static void setFirstRun(boolean enable) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean(SHARED_PREFERENCES_FIRST_START, enable);
        e.apply();
    }

    public static void setUser(String email, String password) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SHARED_PREFERENCES_USER_EMAIL, email);
        editor.putString(SHARED_PREFERENCES_USER_PASSWORD, password);
        editor.apply();
    }

    public static String getEmail() {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0); // 0 - for private mode
        return pref.getString(SHARED_PREFERENCES_USER_EMAIL, "");
    }

    public static String getPassword() {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0); // 0 - for private mode
        return pref.getString(SHARED_PREFERENCES_USER_PASSWORD, "");
    }

    public static void clearUser() {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SHARED_PREFERENCES_USER_EMAIL, "");
        editor.putString(SHARED_PREFERENCES_USER_PASSWORD, "");
        editor.apply();
    }

    public static void setNightMode(int mode) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = getPrefs.edit();
        e.putInt(SHARED_PREFERENCES_NIGHT_MODE, mode);
        e.apply();
    }

    public static int getNightMode() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SHARED_PREFERENCES_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_AUTO);
    }
}
