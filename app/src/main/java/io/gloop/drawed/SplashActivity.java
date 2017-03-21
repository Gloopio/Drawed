package io.gloop.drawed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.UUID;

import io.gloop.Gloop;
import io.gloop.drawed.utils.NameUtil;

public class SplashActivity extends Activity {

    public static final String SHARED_PREFERENCES_NAME = "user";
    public static final String SHARED_PREFERENCES_USER_NAME = "user_name";
    public static final String SHARED_PREFERENCES_USER_PASSWORD = "user_password";
    public static final String SHARED_PREFERENCES_FIRST_START = "firstStart";

    private static final String HOST_URL = "192.168.0.16:8080";
//    private static final String HOST_URL = "52.169.152.13:8080";
//    private static final String API_KEY = "2054bba8-c712-4e91-ba08-f14dad98d814";
    private static final String API_KEY = "abf36d11-d40d-44bb-bf2f-22a70dcf5380";
    private static final boolean DEBUG = true;

    /**
     * Duration of wait
     **/
    private static final int SPLASH_DISPLAY_LENGTH = 1000;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splashscreen);
    }

    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (isFirstStart())
            showIntroOnFirstRun();
        else {

            // New Handler to start the next Activity and close this SplashActivity-Screen after some seconds.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // setup Gloop
                    new Gloop(SplashActivity.this, API_KEY, HOST_URL);

                    if (!logInWithRememberedUser()) {

                        // repeat register until user name does not exists
                        String username = NameUtil.randomUserName(getApplicationContext());
                        final String password = UUID.randomUUID().toString();
                        while (!Gloop.register(username, password)) {
                            username = NameUtil.randomUserName(getApplicationContext());
                        }

                        saveUserCredentialsToSharedPrefs(username, password);
                    }
                    Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                    startActivity(i);
                    finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }
    }

    private boolean logInWithRememberedUser() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        final String username = pref.getString(SHARED_PREFERENCES_USER_NAME, "");
        final String password = pref.getString(SHARED_PREFERENCES_USER_PASSWORD, "");

        if (!username.isEmpty() && !password.isEmpty()) {
            if (Gloop.login(username, password)) {
                saveUserCredentialsToSharedPrefs(username, password);
                return true;
            }
        }
        return false;
    }

    private void saveUserCredentialsToSharedPrefs(final String username, final String password) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SHARED_PREFERENCES_USER_NAME, username);
        editor.putString(SHARED_PREFERENCES_USER_PASSWORD, password);
        editor.apply();
    }

    private boolean isFirstStart() {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return getPrefs.getBoolean(SHARED_PREFERENCES_FIRST_START, true);
    }

    private void showIntroOnFirstRun() {
        //  Declare a new thread to do a preference check
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                //  If the activity has never started before...
                if (isFirstStart()) {

                    //  Launch app intro
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean(SHARED_PREFERENCES_FIRST_START, false);
                    e.apply();
                }
            }
        }).start();
    }
}