package io.gloop.drawed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.gloop.Gloop;
import io.gloop.drawed.utils.NameUtil;
import io.gloop.drawed.utils.ScreenUtil;

public class SplashActivity extends Activity {

    public static final String SHARED_PREFERENCES_FIRST_START = "firstStart";

    public static final String HOST_URL = "52.169.152.13:8080";
    public static final String API_KEY = "f42db1ff-a23d-4921-b420-f1e6c9c03ee5";
    private static final boolean DEBUG = true;

    /**
     * Duration of wait
     **/
    private static final int SPLASH_DISPLAY_LENGTH = 500;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
//    TODO exclude Fabric.io for now because it is producing errors within ndk.
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splashscreen);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // setup screen util at start
        ScreenUtil.setActivity(this);

        if (isFirstStart())
            showIntroOnFirstRun();
        else {

            // New Handler to start the next Activity and close this SplashActivity-Screen after some seconds.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // setup Gloop
                    new Gloop(SplashActivity.this, API_KEY, HOST_URL);

                    if (!Gloop.loginWithRememberedUser()) {
                        // repeat register until user name does not exists
                        String username = NameUtil.randomUserName(getApplicationContext());
                        final String password = UUID.randomUUID().toString();
                        while (!Gloop.register(username, password, true)) {
                            username = NameUtil.randomUserName(getApplicationContext());
                        }
                    }

                    Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                    startActivity(i);
                    finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }
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