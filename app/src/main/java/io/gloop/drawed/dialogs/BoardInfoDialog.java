package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.gloop.drawed.R;
import io.gloop.drawed.deeplink.DeepLinkActivity;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.UserInfo;
import io.gloop.permissions.GloopPermission;
import io.gloop.permissions.GloopUser;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class BoardInfoDialog {

    private double x, y;

    public BoardInfoDialog(final @NonNull Context context, final GloopUser owner, final Board board, final UserInfo userInfo, double x, double y) {
        this.x = x;
        this.y = y;

        final View dialogView = View.inflate(context, R.layout.dialog_info, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);


        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.pop_stat_view);
        layout.setBackgroundColor(board.getColor());

        TextView tvBoardName = (TextView) dialog.findViewById(R.id.dialog_info_board_name);
        tvBoardName.setText(board.getName());

        Switch switchPrivate = (Switch) dialog.findViewById(R.id.dialog_info_switch_private);
        switchPrivate.setChecked(board.isPrivateBoard());
        if (!GloopPermission.hasPermission(board, GloopPermission.WRITE))
            switchPrivate.setEnabled(false);
        switchPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                board.setPrivateBoard(isChecked);
                board.saveInBackground();
            }
        });

        Switch switchFreeze = (Switch) dialog.findViewById(R.id.dialog_info_switch_freeze);
        switchFreeze.setChecked(board.isFreezeBoard());
        if (!GloopPermission.hasPermission(board, GloopPermission.WRITE))
            switchFreeze.setEnabled(false);
        switchFreeze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                board.setFreezeBoard(isChecked);
                board.saveInBackground();
            }
        });

        Button shareButton = (Button) dialog.findViewById(R.id.dialog_info_btn_share);
//            if (owner.getName().equals(board.getGloopUser()))
//                shareButton.setVisibility(View.VISIBLE);
//            else
//                shareButton.setVisibility(View.GONE);

        final ImageButton qrCodeButton = (ImageButton) dialog.findViewById(R.id.dialog_info_btn_qr);
        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new QRCodeDialog(context, board, qrCodeButton);
                revealShow(dialogView, false, dialog);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(context, owner.getName(), board);
                revealShow(dialogView, false, dialog);
            }
        });


        Button deleteButton = (Button) dialog.findViewById(R.id.dialog_info_btn_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // remove user from members and save changes.
                board.removeMemeber(userInfo.getEmail());
                board.save();
                // delete board
                board.delete();
                revealShow(dialogView, false, dialog);
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_info_board_closeDialogImg);
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

        final View view = dialogView.findViewById(R.id.pop_stat_view);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) x;
        int cy = (int) y + 250;

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

    private static void share(Context context, String username, Board board) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = username + " want'ss to share the board " + board.getName() + " with you. " + DeepLinkActivity.BASE_DEEP_LINK + board.getName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Drawed Board Invite");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private static void qrCode(Context context, String username, Board board) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = username + " want'ss to share the board " + board.getName() + " with you. " + DeepLinkActivity.BASE_DEEP_LINK + board.getName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Drawed Board Invite");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}