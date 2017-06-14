package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.gloop.drawed.DrawingView;
import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class ClearBoardDialog extends Dialog {

    public ClearBoardDialog(@NonNull Context context, final DrawingView drawView) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_clear_lines);

        TextView textView = (TextView) findViewById(R.id.dialog_clear_lines_text);
        textView.setText(R.string.clear_lines_of_board);

        //grant access
        Button grantButton = (Button) findViewById(R.id.dialog_clear_lines_ok);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.startNew();
                dismiss();
            }
        });
        // deny access
        Button denyButton = (Button) findViewById(R.id.dialog_clear_lines_cancel);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
