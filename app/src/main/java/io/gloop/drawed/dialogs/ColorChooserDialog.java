package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class ColorChooserDialog {

    private View trigger;

    public ColorChooserDialog(Context context, BoardDetailFragment fragment, View trigger) {
        this.trigger = trigger;
        final View dialogView = View.inflate(context, R.layout.dialog_color_chooser, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);


        //color buttons
        ImageButton color1Btn = (ImageButton) dialog.findViewById(R.id.color1);
        color1Btn.setOnClickListener(fragment.new ColorChangeListener(color1Btn, dialog));
        ImageButton color2Btn = (ImageButton) dialog.findViewById(R.id.color2);
        color2Btn.setOnClickListener(fragment.new ColorChangeListener(color2Btn, dialog));
        ImageButton color3Btn = (ImageButton) dialog.findViewById(R.id.color3);
        color3Btn.setOnClickListener(fragment.new ColorChangeListener(color3Btn, dialog));
        ImageButton color4Btn = (ImageButton) dialog.findViewById(R.id.color4);
        color4Btn.setOnClickListener(fragment.new ColorChangeListener(color4Btn, dialog));
        ImageButton color5Btn = (ImageButton) dialog.findViewById(R.id.color5);
        color5Btn.setOnClickListener(fragment.new ColorChangeListener(color5Btn, dialog));
        ImageButton color6Btn = (ImageButton) dialog.findViewById(R.id.color6);
        color6Btn.setOnClickListener(fragment.new ColorChangeListener(color6Btn, dialog));
        ImageButton color7Btn = (ImageButton) dialog.findViewById(R.id.color7);
        color7Btn.setOnClickListener(fragment.new ColorChangeListener(color7Btn, dialog));
        ImageButton color8Btn = (ImageButton) dialog.findViewById(R.id.color8);
        color8Btn.setOnClickListener(fragment.new ColorChangeListener(color8Btn, dialog));
        ImageButton color9Btn = (ImageButton) dialog.findViewById(R.id.color9);
        color9Btn.setOnClickListener(fragment.new ColorChangeListener(color9Btn, dialog));
        ImageButton color10Btn = (ImageButton) dialog.findViewById(R.id.color10);
        color10Btn.setOnClickListener(fragment.new ColorChangeListener(color10Btn, dialog));
        ImageButton color11Btn = (ImageButton) dialog.findViewById(R.id.color11);
        color11Btn.setOnClickListener(fragment.new ColorChangeListener(color11Btn, dialog));
        ImageButton color12Btn = (ImageButton) dialog.findViewById(R.id.color12);
        color12Btn.setOnClickListener(fragment.new ColorChangeListener(color12Btn, dialog));

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_color_chooser_closeDialogImg);
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

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.pop_color_chooser);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = trigger.getRight() / 2;
        int cy = trigger.getBottom();

        if (b) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

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

