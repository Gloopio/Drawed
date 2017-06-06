package io.gloop.drawed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.gloop.Gloop;
import io.gloop.drawed.utils.NameUtil;
import io.gloop.drawed.utils.ScreenUtil;

public class SplashActivity extends Activity {

    private static final String SHARED_PREFERENCES_FIRST_START = "firstStart";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
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
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // setup Gloop
                    // new Gloop(SplashActivity.this, API_KEY, HOST_URL);

                    // or set apiKey and host in the AndroidManifest.xml file.
                    new Gloop(SplashActivity.this);

                    if (!Gloop.loginWithRememberedUser()) {
                        // repeat register until user name does not exists
                        final String password = UUID.randomUUID().toString();
                        while (!Gloop.register(NameUtil.randomUserName(getApplicationContext()), password, true)) {
                        }
                    }

                    Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                    startActivity(i);
                    finish();
                }
            }).start();
        }
    }

    private boolean isFirstStart() {
        return PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(SHARED_PREFERENCES_FIRST_START, true);
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