package io.gloop.drawed;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.gloop.drawed.dialogs.ClearBoardDialog;
import io.gloop.drawed.dialogs.ColorChooserDialog;
import io.gloop.drawed.dialogs.LineThicknessChooserDialog;
import io.gloop.drawed.model.Board;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link BoardListActivity}
 * in two-pane mode (on tablets) or a {@link BoardDetailActivity}
 * on handsets.
 */
public class BoardDetailFragment extends Fragment implements BottomNavigation.OnMenuItemSelectionListener {

    public static final String ARG_BOARD = "board";

    private DrawingView drawView;
    private String currentColor = "#FF000000";
    public static int smallBrush, mediumBrush, largeBrush;
    private BottomNavigation navigation;

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

        navigation = (BottomNavigation) rootView.findViewById(R.id.BottomNavigation);
        navigation.setOnMenuItemClickListener(this);
        navigation.setSelectedIndex(2, true);

        return rootView;
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position, final boolean fromUser) {
        onDrawingMenuSelected(itemId, position, fromUser);
    }

    @Override
    public void onMenuItemReselect(final int itemId, final int position, final boolean fromUser) {
        onDrawingMenuSelected(itemId, position, fromUser);
    }

    public void onDrawingMenuSelected(final int itemId, final int position, final boolean fromUser) {
        switch (itemId) {
            case R.id.nav_darwing_clear:
                new ClearBoardDialog(BoardDetailFragment.this.getContext(), drawView, navigation);
                break;
            case R.id.nav_darwing_brush:
                drawView.setErase(false);
                break;
            case R.id.nav_darwing_delete_line:
                drawView.setErase(true);
                break;
            case R.id.nav_darwing_line_thickness:
                new LineThicknessChooserDialog(BoardDetailFragment.this.getContext(), drawView, navigation);
                break;
            case R.id.nav_drawing_color:
                new ColorChooserDialog(getContext(), BoardDetailFragment.this, navigation);
                break;
        }
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
            dialog.dismiss();
        }
    }
}
