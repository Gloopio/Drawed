package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.glxn.qrgen.android.QRCode;

import io.gloop.drawed.R;
import io.gloop.drawed.deeplink.DeepLinkActivity;
import io.gloop.drawed.model.BoardInfo;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class QRCodeDialog {

    private ImageButton trigger;

    public QRCodeDialog(final @NonNull Context context, BoardInfo boardInfo, ImageButton trigger) {
        this.trigger = trigger;
        final View dialogView = View.inflate(context, R.layout.dialog_qr_code, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.pop_qr_code);
        layout.setBackgroundColor(boardInfo.getColor());

        Bitmap myBitmap = QRCode.from(DeepLinkActivity.BASE_DEEP_LINK + boardInfo.getName()).withSize(500, 500).withColor(Color.WHITE, boardInfo.getColor()).bitmap();
        ImageView myImage = (ImageView) dialog.findViewById(R.id.dialog_qr_image_view);
        myImage.setImageBitmap(myBitmap);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_qr_code_closeDialogImg);
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

        final View view = dialogView.findViewById(R.id.pop_qr_code);

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
