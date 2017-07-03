package io.gloop.drawed;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import io.gloop.drawed.dialogs.ClearBoardDialog;
import io.gloop.drawed.dialogs.ColorChooserDialog;
import io.gloop.drawed.dialogs.LineThicknessChooserDialog;
import io.gloop.drawed.model.Board;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link BoardListActivity}
 * in two-pane mode (on tablets) or a {@link BoardDetailActivity}
 * on handsets.
 */
public class BoardDetailFragment extends Fragment {

    public static final String ARG_BOARD = "board";

    private DrawingView drawView;
    private String currentColor = "#FF000000";
    public static int smallBrush, mediumBrush, largeBrush;
    private ImageView changeColorButton;

    private Board board;

    public BoardDetailFragment() {
        // Mandatory empty constructor for the fragment manager to instantiate the fragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_BOARD)) {
            board = (Board) getArguments().getSerializable(ARG_BOARD);
        }

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.drawing_view, container, false);

        //get drawing viewl
        drawView = (DrawingView) rootView.findViewById(R.id.drawing);
        if (board != null)
            drawView.setBoard(board);

        // set default color
        drawView.setColor(currentColor);

        //set initial size
        drawView.setBrushSize(smallBrush);

        changeColorButton = (ImageView) rootView.findViewById(R.id.draw_view_btn_change_color);
        changeColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorChooserDialog(BoardDetailFragment.this).show();
            }
        });

        ImageView changeLineThickness = (ImageView) rootView.findViewById(R.id.draw_view_btn_change_line_thickness);
        changeLineThickness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LineThicknessChooserDialog(BoardDetailFragment.this.getContext(), drawView).show();
            }
        });

        ImageView deleteImage = (ImageView) rootView.findViewById(R.id.draw_view_btn_delete_lines);
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ClearBoardDialog(BoardDetailFragment.this.getContext(), drawView).show();
            }
        });

        final ImageView brush = (ImageView) rootView.findViewById(R.id.draw_view_btn_brush);
        brush.setVisibility(View.GONE);
        final ImageView erase = (ImageView) rootView.findViewById(R.id.draw_view_btn_eraser);

        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.setErase(true);
                brush.setVisibility(View.VISIBLE);
                erase.setVisibility(View.GONE);
            }
        });

        brush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.setErase(false);
                erase.setVisibility(View.VISIBLE);
                brush.setVisibility(View.GONE);
            }
        });


        return rootView;
    }

    public class ColorChangeListener implements View.OnClickListener {
        private final ImageButton imgView;
        private final Dialog dialog;

        public ColorChangeListener(ImageButton imgView, Dialog dialog) {
            this.imgView = imgView;
            this.dialog = dialog;

            // set currently selected color
            if (this.imgView.getTag().toString().equals(currentColor))
                if (currentColor.equals("#FFFFFFFF"))   // white
                    this.imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed_black));
                else
                    this.imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        }

        @Override
        public void onClick(View view) {
            // get selected color and set to drawView
            currentColor = view.getTag().toString();
            drawView.setColor(currentColor);

            // change color of icon
            Drawable myIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_color_lens_black_24dp, null);
            if (myIcon != null)
                myIcon.setColorFilter(Color.parseColor(currentColor), PorterDuff.Mode.SRC_ATOP);

            changeColorButton.setImageDrawable(myIcon);

            // set button to selected
            if (currentColor.equals("#FFFFFFFF"))   // white
                this.imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed_black));
            else
                this.imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

            // close dialog
            dialog.dismiss();
        }
    }
}
