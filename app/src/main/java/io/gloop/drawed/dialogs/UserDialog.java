package io.gloop.drawed.dialogs;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.R;
import io.gloop.permissions.GloopUser;

import static io.gloop.drawed.SplashActivity.SHARED_PREFERENCES_FIRST_START;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class UserDialog extends Dialog {

    public UserDialog(final @NonNull Context context, final GloopUser owner) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_user);

        TextView username = (TextView) findViewById(R.id.dialog_user_name);
        username.setText(owner.getName());

        Button cancelButton = (Button) findViewById(R.id.dialog_user_btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Button logoutButton = (Button) findViewById(R.id.dialog_user_btn_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gloop.logout();
                resetShowIntroOnNextStart(context);

                dismiss();

//                doRestart(context);
                System.exit(0);
            }
        });

    }

    private static void resetShowIntroOnNextStart(final @NonNull Context context) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean(SHARED_PREFERENCES_FIRST_START, false);
        e.apply();
    }

    private static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        GloopLogger.e("Was not able to restart application, mStartActivity null");
                    }
                } else {
                    GloopLogger.e("Was not able to restart application, PM null");
                }
            } else {
                GloopLogger.e("Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            GloopLogger.e("Was not able to restart application");
        }
    }
}
