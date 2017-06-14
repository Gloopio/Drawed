package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.DrawingView;
import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class LineThicknessChooserDialog extends Dialog {

    public LineThicknessChooserDialog(final @NonNull Context context, final DrawingView drawView) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_line_thickness_chooser);

        //listen for clicks on size buttons
        ImageButton smallBtn = (ImageButton) findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(BoardDetailFragment.smallBrush);
                dismiss();
            }
        });

        ImageButton mediumBtn = (ImageButton) findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(BoardDetailFragment.mediumBrush);
                dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton) findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(BoardDetailFragment.largeBrush);
                dismiss();
            }
        });

    }
}