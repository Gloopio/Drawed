package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.gloop.drawed.R;
import io.gloop.drawed.deeplink.DeepLinkActivity;
import io.gloop.drawed.model.Board;
import io.gloop.permissions.GloopUser;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class BoardInfoDialog extends Dialog {

    public BoardInfoDialog(final @NonNull Context context, final GloopUser owner, final Board board) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_info);

        LinearLayout layout = (LinearLayout) findViewById(R.id.pop_stat_view);
        layout.setBackgroundColor(board.getColor());

        TextView tvBoardName = (TextView) findViewById(R.id.dialog_info_board_name);
        tvBoardName.setText(board.getName());

        Switch switchPrivate = (Switch) findViewById(R.id.dialog_info_switch_private);
        switchPrivate.setChecked(board.isPrivateBoard());
        switchPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                board.setPrivateBoard(isChecked);
                board.saveInBackground();
            }
        });

        Switch switchFreeze = (Switch) findViewById(R.id.dialog_info_switch_freeze);
        switchFreeze.setChecked(board.isFreezeBoard());
        switchFreeze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                board.setFreezeBoard(isChecked);
                board.saveInBackground();
            }
        });

        Button shareButton = (Button) findViewById(R.id.dialog_info_btn_share);
//            if (owner.getName().equals(board.getGloopUser()))
//                shareButton.setVisibility(View.VISIBLE);
//            else
//                shareButton.setVisibility(View.GONE);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(context, owner.getName(), board);
                dismiss();
            }
        });

        Button dialogButton = (Button) findViewById(R.id.dialog_info_btn_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button deleteButton = (Button) findViewById(R.id.dialog_info_btn_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                board.delete();
                dismiss();
            }
        });
    }

    private static void share(Context context, String username, Board board) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = username + " want'ss to share the board " + board.getName() + " with you. " + DeepLinkActivity.BASE_DEEP_LINK + board.getName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Drawed Board Invite");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}