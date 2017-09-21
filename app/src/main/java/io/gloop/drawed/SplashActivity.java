package io.gloop.drawed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import io.fabric.sdk.android.Fabric;
import io.gloop.Gloop;
import io.gloop.drawed.utils.ScreenUtil;
import io.gloop.drawed.utils.SharedPreferencesStore;

public class SplashActivity extends Activity {

    public static final String SHARED_PREFERENCES_FIRST_START = "firstStart";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Fabric.with(this, new Crashlytics());
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_splashscreen);

        SharedPreferencesStore.setContext(getBaseContext());
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // setup screen util at start
        ScreenUtil.setActivity(this);

        if (SharedPreferencesStore.isFirstStart())
            showIntroOnFirstRun();
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // setup Gloop
                    // set apiKey and host in the AndroidManifest.xml file.
                    // It is also possible to pass them as parameters to the initialize method.
                    Gloop.initialize(SplashActivity.this);

//                    // Gloop will login with the remembered user.
//                    // The login and register method both provide a parameter keepSignedIn which can be set to tre. By default it is set to false.
//                    if (!Gloop.loginWithRememberedUser()) {
//                        // repeat register until user name does not exists
//                        final String password = UUID.randomUUID().toString();
//                        // Register a new user with the register method of gloop. By setting keepSignedIn to true the user can be logged in with the loginWithRememberedUser method.
//                        while (!Gloop.register(NameUtil.randomUserName(getApplicationContext()), password, true)) {
//                        }
//                    }


                    if (!logInWithRememberedUser()) {
                        Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        SplashActivity.this.startActivity(mainIntent);
//                        SplashActivity.this.finish();
                    }
//
//                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
//                    startActivity(i);
//                    finish();
                }
            }).start();
        }
    }

    private boolean logInWithRememberedUser() {
        String email = SharedPreferencesStore.getEmail();
        String password = SharedPreferencesStore.getPassword();

        if (!email.isEmpty() && !password.isEmpty()) {
            if (Gloop.login(email, password)) {
                Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(i);
                finish();
                return true;
            }
        }
        return false;
    }

    private void showIntroOnFirstRun() {
        //  Declare a new thread to do a preference check
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  If the activity has never started before...
                if (SharedPreferencesStore.isFirstStart()) {

                    //  Launch app intro
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferencesStore.setFirstRun(false);
                }
            }
        }).start();
    }
}