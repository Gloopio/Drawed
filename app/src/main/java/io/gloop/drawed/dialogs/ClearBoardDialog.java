package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.gloop.drawed.DrawingView;
import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class ClearBoardDialog {

    private View trigger;

    public ClearBoardDialog(@NonNull Context context, final DrawingView drawView, View trigger) {
        this.trigger = trigger;
        final View dialogView = View.inflate(context, R.layout.dialog_clear_lines, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);


        TextView textView = (TextView) dialog.findViewById(R.id.dialog_clear_lines_text);
        textView.setText(R.string.clear_lines_of_board);

        //grant access
        Button grantButton = (Button) dialog.findViewById(R.id.dialog_clear_lines_ok);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.startNew();
                revealShow(dialogView, false, dialog);
            }
        });
        // deny access
        Button denyButton = (Button) dialog.findViewById(R.id.dialog_clear_lines_cancel);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revealShow(dialogView, false, dialog);
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_clear_lines_closeDialogImg);
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

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.pop_clear_lines);

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
