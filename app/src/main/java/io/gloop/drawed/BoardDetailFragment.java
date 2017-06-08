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
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

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
    private int smallBrush, mediumBrush, largeBrush;
    private ImageView changeColorButton;

    private Board board;
    private SaveInBackgroundWorker worker;

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
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        //get drawing view
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
                showChangeColorPopup();
            }
        });

        ImageView changeLineThickness = (ImageView) rootView.findViewById(R.id.draw_view_btn_change_line_thickness);
        changeLineThickness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLineThicknessPopup();
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // opens a dialog on long press on the list item
    private void showChangeColorPopup() {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_chooser_color);

        //color buttons
        ImageButton color1Btn = (ImageButton) dialog.findViewById(R.id.color1);
        color1Btn.setOnClickListener(new ColorChangeListener(color1Btn, dialog));
        ImageButton color2Btn = (ImageButton) dialog.findViewById(R.id.color2);
        color2Btn.setOnClickListener(new ColorChangeListener(color2Btn, dialog));
        ImageButton color3Btn = (ImageButton) dialog.findViewById(R.id.color3);
        color3Btn.setOnClickListener(new ColorChangeListener(color3Btn, dialog));
        ImageButton color4Btn = (ImageButton) dialog.findViewById(R.id.color4);
        color4Btn.setOnClickListener(new ColorChangeListener(color4Btn, dialog));
        ImageButton color5Btn = (ImageButton) dialog.findViewById(R.id.color5);
        color5Btn.setOnClickListener(new ColorChangeListener(color5Btn, dialog));
        ImageButton color6Btn = (ImageButton) dialog.findViewById(R.id.color6);
        color6Btn.setOnClickListener(new ColorChangeListener(color6Btn, dialog));
        ImageButton color7Btn = (ImageButton) dialog.findViewById(R.id.color7);
        color7Btn.setOnClickListener(new ColorChangeListener(color7Btn, dialog));
        ImageButton color8Btn = (ImageButton) dialog.findViewById(R.id.color8);
        color8Btn.setOnClickListener(new ColorChangeListener(color8Btn, dialog));
        ImageButton color9Btn = (ImageButton) dialog.findViewById(R.id.color9);
        color9Btn.setOnClickListener(new ColorChangeListener(color9Btn, dialog));
        ImageButton color10Btn = (ImageButton) dialog.findViewById(R.id.color10);
        color10Btn.setOnClickListener(new ColorChangeListener(color10Btn, dialog));
        ImageButton color11Btn = (ImageButton) dialog.findViewById(R.id.color11);
        color11Btn.setOnClickListener(new ColorChangeListener(color11Btn, dialog));
        ImageButton color12Btn = (ImageButton) dialog.findViewById(R.id.color12);
        color12Btn.setOnClickListener(new ColorChangeListener(color12Btn, dialog));

        dialog.show();
    }

    private void showChangeLineThicknessPopup() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_chooser_line_thickness);

        //listen for clicks on size buttons
        ImageButton smallBtn = (ImageButton) dialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(smallBrush);
                dialog.dismiss();
            }
        });

        ImageButton mediumBtn = (ImageButton) dialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(mediumBrush);
                dialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton) dialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(largeBrush);
                dialog.dismiss();
            }
        });

        //show and wait for user interaction
        dialog.show();
    }

    private class ColorChangeListener implements View.OnClickListener {
        private final ImageButton imgView;
        private final Dialog dialog;

        ColorChangeListener(ImageButton imgView, Dialog dialog) {
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
