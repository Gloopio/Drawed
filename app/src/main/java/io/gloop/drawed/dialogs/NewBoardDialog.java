package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.github.clans.fab.FloatingActionMenu;

import io.gloop.drawed.BoardDetailActivity;
import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.drawed.utils.ColorUtil;
import io.gloop.drawed.utils.NameUtil;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopUser;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */
public class NewBoardDialog {

    private FloatingActionMenu fab;

    public NewBoardDialog(@NonNull final Context context, final GloopUser owner, final View view, final boolean mTwoPane, final FragmentManager fragmentManager, FloatingActionMenu fab) {
//        super(context, R.style.AppTheme_PopupTheme);

        this.fab = fab;

        final View dialogView = View.inflate(context, R.layout.dialog_new_board, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);



        final String colorName = NameUtil.randomColor(context);
        final String randomName = NameUtil.randomAdjective(context) + colorName + NameUtil.randomObject(context);

        final EditText etBoardName = (EditText) dialog.findViewById(R.id.dialog_new_board_board_name);
        etBoardName.setText(randomName);

        Button closeButton = (Button) dialog.findViewById(R.id.dialog_new_board_btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revealShow(dialogView, false, dialog);
            }
        });

        Button saveButton = (Button) dialog.findViewById(R.id.dialog_new_board_btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progress = new ProgressDialog(context);
                progress.setTitle("Creating new board");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();

                final Board board = new Board();

                // test to grant additional permission to another user
//                board.addPermission("test", 1000);

                // set name and color
                board.setName(etBoardName.getText().toString());
                board.setColor(ColorUtil.getColorByName(context, colorName));

                // set board private
                Switch switchPrivate = (Switch) dialog.findViewById(R.id.dialog_new_board_switch_private);
                board.setPrivateBoard(switchPrivate.isChecked());

                // set board freeze
                Switch switchFreeze = (Switch) dialog.findViewById(R.id.dialog_new_board_switch_freeze);
                board.setFreezeBoard(switchFreeze.isChecked());

                GloopGroup group = new GloopGroup();
                group.setUser(owner.getUserId(), PUBLIC | READ | WRITE);

                // set permissions depending on the selection.
                if (board.isPrivateBoard()) {
                    group.setUser(owner.getUserId(),  READ | WRITE);
                    if (board.isFreezeBoard())
                        board.setUser(group.getObjectId(), READ);
                    else
                        board.setUser(group.getObjectId(), READ | WRITE);
                } else if (board.isFreezeBoard()) {
                    if (board.isPrivateBoard()) {
                        group.setUser(owner.getUserId(), READ | WRITE);
                        board.setUser(group.getObjectId(), READ);
                    } else
                        board.setUser(group.getObjectId(), READ | PUBLIC);
                } else {
                    board.setUser(group.getObjectId(), READ | WRITE | PUBLIC);
                }

                group.save();

                if (board.isPrivateBoard()) {
                    // this is used to discover private boards and request access to it.
                    PrivateBoardRequest privateBoard = new PrivateBoardRequest();
                    privateBoard.setUser(board.getOwner(), READ | WRITE | PUBLIC);
                    privateBoard.setBoardName(board.getName());
                    privateBoard.setBoardCreator(owner.getUserId());
                    privateBoard.setGroupId(group.getObjectId());
                    privateBoard.save();
                }


                // save the created board
                board.save();

                // open the board in detail fragment
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(BoardDetailFragment.ARG_BOARD, board);
                    BoardDetailFragment fragment = new BoardDetailFragment();
                    fragment.setArguments(arguments);
                    fragmentManager.beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, BoardDetailActivity.class);
                    intent.putExtra(BoardDetailFragment.ARG_BOARD, board);

                    context.startActivity(intent);
                }

                progress.dismiss();

                // close popup
                dialog.dismiss();
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_new_board_closeDialogImg);
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

        final View view = dialogView.findViewById(R.id.pop_new_board);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = fab.getRight()- 100;
        int cy = fab.getBottom() -300;

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
}
