package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.R;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.permissions.GloopGroup;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class AcceptBoardAccessDialog extends Dialog {

    public AcceptBoardAccessDialog(@NonNull Context context,  final BoardAccessRequest request) {
        super(context, R.style.AppTheme_PopupTheme);
        GloopLogger.i("Show access user popup.");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_acceped_board_access);

        TextView textView = (TextView) findViewById(R.id.dialog_accept_text);
        textView.setText("Allow access to user " + request.getUserId() + " on board " + request.getBoardName());

        //grant access
        Button grantButton = (Button) findViewById(R.id.dialog_accept_btn_grant);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GloopGroup group = Gloop
                        .all(GloopGroup.class)
                        .where()
                        .equalsTo("objectId", request.getBoardGroupId())
                        .first();
                group.addMember(request.getUserId());
                group.save();

                request.delete();

                dismiss();
            }
        });
        // deny access
        Button denyButton = (Button) findViewById(R.id.dialog_accept_btn_deny);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.delete();
                dismiss();
            }
        });
    }

}
