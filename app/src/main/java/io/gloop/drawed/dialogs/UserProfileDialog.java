package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.GloopLogger;
import io.gloop.drawed.R;
import io.gloop.drawed.model.UserInfo;

/**
 * Created by Alex Untertrifaller on 21.09.17.
 */

public class UserProfileDialog {

    private Activity activity;
    private CircleImageView userImage;
    private UserInfo userInfo;

    public UserProfileDialog(Activity activity, CircleImageView userImage, UserInfo userInfo) {
        this.activity = activity;
        this.userImage = userImage;
        this.userInfo = userInfo;

        showUserProfileDialog();
    }

    private void showUserProfileDialog() {

        final View dialogView = View.inflate(activity, R.layout.dialog_user_profile, null);

        final Dialog dialog = new Dialog(activity, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

//        Button button = (Button) dialog.findViewById(R.id.image_chooser);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                activity.startActivityForResult(photoPickerIntent, BoardListActivity.SELECT_PHOTO);
//            }
//        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.closeDialogImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });

        CircleImageView profileImage = (CircleImageView) dialog.findViewById(R.id.profile_user_image);
        Picasso.with(activity.getApplicationContext())
                .load(userInfo.getImageURL())
                .into(profileImage);

        TextView profileUserName = (TextView) dialog.findViewById(R.id.profile_user_name);
        profileUserName.setText(userInfo.getUserName());

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

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) (userImage.getX() + (userImage.getWidth() / 2));
        int cy = (int) (userImage.getY()) + userImage.getHeight() + 56;


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
