package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;

import io.gloop.drawed.R;
import io.gloop.drawed.utils.SharedPreferencesStore;

/**
 * Created by Alex Untertrifaller on 21.09.17.
 */

public class DayNightSettingsDialog implements View.OnClickListener {

    private Dialog dialog;
    private AppCompatActivity activity;

    public DayNightSettingsDialog(AppCompatActivity activity) {
        this.activity = activity;

        final View dialogView = View.inflate(activity, R.layout.dialog_day_night_settings, null);

        dialog = new Dialog(activity, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        setupNightModeButtons(dialogView);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.settings_night_mode_closeDialogImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {

                    revealShow(dialogView, false, dialog);
                    return true;
                }

                return false;
            }
        });

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }


    private void setupNightModeButtons(View view) {
        view.findViewById(R.id.settings_night_mode_auto).setOnClickListener(this);
        view.findViewById(R.id.settings_night_mode_no).setOnClickListener(this);
        view.findViewById(R.id.settings_night_mode_yes).setOnClickListener(this);
        view.findViewById(R.id.settings_night_mode_follow_system).setOnClickListener(this);

        RadioButton rb;

        switch (SharedPreferencesStore.getNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                rb = (RadioButton) view.findViewById(R.id.settings_night_mode_follow_system);
                rb.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                rb = (RadioButton) view.findViewById(R.id.settings_night_mode_no);
                rb.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rb = (RadioButton) view.findViewById(R.id.settings_night_mode_yes);
                rb.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                rb = (RadioButton) view.findViewById(R.id.settings_night_mode_auto);
                rb.setChecked(true);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.settings_night_mode_follow_system:
                if (checked) {
                    SharedPreferencesStore.setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                break;
            case R.id.settings_night_mode_no:
                if (checked) {
                    SharedPreferencesStore.setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;
            case R.id.settings_night_mode_yes:
                if (checked) {
                    SharedPreferencesStore.setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                break;
            case R.id.settings_night_mode_auto:
                if (checked) {
                    SharedPreferencesStore.setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                    setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                }
                break;

        }
    }

    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);
        activity.recreate();
        dialog.dismiss();
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.settings_night_mode_dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = w / 2;
        int cy = h / 2;


        if (b) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(700);
            anim.start();
        }
    }
}
