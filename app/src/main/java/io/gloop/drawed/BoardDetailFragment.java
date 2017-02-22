package io.gloop.drawed;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class BoardDetailFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_BOARD = "board";

    private Board board;

    private DrawingView drawView;

    private ImageButton currPaint;

    private String currentColor;

    private float smallBrush, mediumBrush, largeBrush;

    public BoardDetailFragment() {
        // Mandatory empty constructor for the fragment manager to instantiate the fragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_BOARD)) {
            board = (Board) getArguments().getSerializable(ARG_BOARD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        //get drawing view
        drawView = (DrawingView) rootView.findViewById(R.id.drawing);
        if (board != null)
            drawView.setBoard(board);

        //get the palette and first color button
//        LinearLayout paintLayout = (LinearLayout) rootView.findViewById(R.id.paint_colors);
//        currPaint = (ImageButton) paintLayout.getChildAt(0);
//        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        currentColor = "#FF000000";
        drawView.setColor(currentColor);

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //set initial size
        drawView.setBrushSize(smallBrush);

        ImageView changeColorButton = (ImageView) rootView.findViewById(R.id.draw_view_btn_change_color);
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


//        //draw button
//        ImageButton drawBtn = (ImageButton) rootView.findViewById(R.id.draw_btn);
//        drawBtn.setOnClickListener(this);
//
//
//        //erase button
//        ImageButton eraseBtn = (ImageButton) rootView.findViewById(R.id.erase_btn);
//        eraseBtn.setOnClickListener(this);
//
//        //new button
//        ImageButton newBtn = (ImageButton) rootView.findViewById(R.id.new_btn);
//        newBtn.setOnClickListener(this);
//
//        //save button
//        ImageButton saveBtn = (ImageButton) rootView.findViewById(R.id.save_btn);
//        saveBtn.setOnClickListener(this);

        return rootView;
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

    @SuppressWarnings("deprecation")
    private class ColorChangeListener implements View.OnClickListener {
        private ImageButton imgView;
        private Dialog dialog;

        ColorChangeListener(ImageButton imgView, Dialog dialog) {
            this.imgView = imgView;
            this.dialog = dialog;
            // set currently selected color
            if (this.imgView.getTag().toString().equals(currentColor))
                this.imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        }

        @Override
        public void onClick(View view) {
            currentColor = view.getTag().toString();

            drawView.setColor(currentColor);

            // set selected color
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            dialog.dismiss();
        }
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
                drawView.setLastBrushSize(smallBrush);
                dialog.dismiss();
            }
        });


        ImageButton mediumBtn = (ImageButton) dialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(mediumBrush);
                drawView.setLastBrushSize(mediumBrush);
                dialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton) dialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(largeBrush);
                drawView.setLastBrushSize(largeBrush);
                dialog.dismiss();
            }
        });

        //set selected line thickness
        // TODO impl
//        if (drawView.getLastBrushSize() == smallBrush)
//            smallBtn.setSelected(true);
//        else if (drawView.getLastBrushSize() == mediumBrush)
//            mediumBtn.setSelected(true);
//        else if (drawView.getLastBrushSize() == largeBrush)
//            largeBtn.setSelected(true);

        //show and wait for user interaction
        dialog.show();
    }


    @Override
    public void onClick(View view) {

//        if (view.getId() == R.id.draw_btn) {
//            //draw button clicked
//            final Dialog brushDialog = new Dialog(getContext());
//            brushDialog.setTitle("Brush size:");
//            brushDialog.setContentView(R.layout.popup_chooser_line_thickness);
//            //listen for clicks on size buttons
//            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
//            smallBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(false);
//                    drawView.setBrushSize(smallBrush);
//                    drawView.setLastBrushSize(smallBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
//            mediumBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(false);
//                    drawView.setBrushSize(mediumBrush);
//                    drawView.setLastBrushSize(mediumBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
//            largeBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(false);
//                    drawView.setBrushSize(largeBrush);
//                    drawView.setLastBrushSize(largeBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            //show and wait for user interaction
//            brushDialog.show();
//        } else if (view.getId() == R.id.erase_btn) {
//            //switch to erase - choose size
//            final Dialog brushDialog = new Dialog(getContext());
//            brushDialog.setTitle("Eraser size:");
//            brushDialog.setContentView(R.layout.popup_chooser_line_thickness);
//            //size buttons
//            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
//            smallBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(smallBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
//            mediumBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(mediumBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
//            largeBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(largeBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            brushDialog.show();
//        } else if (view.getId() == R.id.new_btn) {
//            //new button
//            AlertDialog.Builder newDialog = new AlertDialog.Builder(getContext());
//            newDialog.setTitle("New drawing");
//            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
//            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    drawView.startNew();
//                    dialog.dismiss();
//                }
//            });
//            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            newDialog.show();
//        } else if (view.getId() == R.id.save_btn) {
//            //save drawing
//            AlertDialog.Builder saveDialog = new AlertDialog.Builder(getContext());
//            saveDialog.setTitle("Save drawing");
//            saveDialog.setMessage("Save drawing to device Gallery?");
//            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    //save drawing
//                    drawView.setDrawingCacheEnabled(true);
//                    //attempt to save
//                    String imgSaved = MediaStore.Images.Media.insertImage(
//                            getActivity().getContentResolver(), drawView.getDrawingCache(),
//                            UUID.randomUUID().toString() + ".png", "drawing");
//                    //feedback
//                    if (imgSaved != null) {
//                        Toast savedToast = Toast.makeText(getContext(),
//                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
//                        savedToast.show();
//                    } else {
//                        Toast unsavedToast = Toast.makeText(getContext(),
//                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
//                        unsavedToast.show();
//                    }
//                    drawView.destroyDrawingCache();
//                }
//            });
//            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            saveDialog.show();
//        }
    }
}
