package io.gloop.drawed.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static io.gloop.drawed.SplashActivity.SHARED_PREFERENCES_FIRST_START;

/**
 * Created by Alex Untertrifaller on 06.09.17.
 */

public class SharedPreferencesStore {

    private static Context context;

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

}
