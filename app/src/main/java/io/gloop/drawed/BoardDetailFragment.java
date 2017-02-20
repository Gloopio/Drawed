package io.gloop.drawed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.UUID;

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
        LinearLayout paintLayout = (LinearLayout) rootView.findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //draw button
        ImageButton drawBtn = (ImageButton) rootView.findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //set initial size
        drawView.setBrushSize(mediumBrush);

        //erase button
        ImageButton eraseBtn = (ImageButton) rootView.findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        ImageButton newBtn = (ImageButton) rootView.findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        ImageButton saveBtn = (ImageButton) rootView.findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //color buttons
        ImageButton color1Btn = (ImageButton) rootView.findViewById(R.id.color1);
        color1Btn.setOnClickListener(new ColorChangeListener(color1Btn));
        ImageButton color2Btn = (ImageButton) rootView.findViewById(R.id.color2);
        color2Btn.setOnClickListener(new ColorChangeListener(color2Btn));
        ImageButton color3Btn = (ImageButton) rootView.findViewById(R.id.color3);
        color3Btn.setOnClickListener(new ColorChangeListener(color3Btn));
        ImageButton color4Btn = (ImageButton) rootView.findViewById(R.id.color4);
        color4Btn.setOnClickListener(new ColorChangeListener(color4Btn));
        ImageButton color5Btn = (ImageButton) rootView.findViewById(R.id.color5);
        color5Btn.setOnClickListener(new ColorChangeListener(color5Btn));
        ImageButton color6Btn = (ImageButton) rootView.findViewById(R.id.color6);
        color6Btn.setOnClickListener(new ColorChangeListener(color6Btn));
        ImageButton color7Btn = (ImageButton) rootView.findViewById(R.id.color7);
        color7Btn.setOnClickListener(new ColorChangeListener(color7Btn));
        ImageButton color8Btn = (ImageButton) rootView.findViewById(R.id.color8);
        color8Btn.setOnClickListener(new ColorChangeListener(color8Btn));
        ImageButton color9Btn = (ImageButton) rootView.findViewById(R.id.color9);
        color9Btn.setOnClickListener(new ColorChangeListener(color9Btn));
        ImageButton color10Btn = (ImageButton) rootView.findViewById(R.id.color10);
        color10Btn.setOnClickListener(new ColorChangeListener(color10Btn));
        ImageButton color11Btn = (ImageButton) rootView.findViewById(R.id.color11);
        color11Btn.setOnClickListener(new ColorChangeListener(color11Btn));
        ImageButton color12Btn = (ImageButton) rootView.findViewById(R.id.color12);
        color12Btn.setOnClickListener(new ColorChangeListener(color12Btn));

        return rootView;
    }

    private class ColorChangeListener implements  View.OnClickListener{
        private ImageButton imgView;

        ColorChangeListener(ImageButton imgView) {
            this.imgView = imgView;
        }

        @Override
        public void onClick(View view) {
            String color = view.getTag().toString();

            drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }

    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.draw_btn) {
            //draw button clicked
            final Dialog brushDialog = new Dialog(getContext());
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            //show and wait for user interaction
            brushDialog.show();
        } else if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(getContext());
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //size buttons
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        } else if (view.getId() == R.id.new_btn) {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(getContext());
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(getContext());
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    //attempt to save
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getActivity().getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString() + ".png", "drawing");
                    //feedback
                    if (imgSaved != null) {
                        Toast savedToast = Toast.makeText(getContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    } else {
                        Toast unsavedToast = Toast.makeText(getContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }
    }
}
