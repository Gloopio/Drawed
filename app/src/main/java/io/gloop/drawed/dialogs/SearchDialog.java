package io.gloop.drawed.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionMenu;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.BoardDetailActivity;
import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.drawed.model.UserInfo;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopUser;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class SearchDialog {

    private Activity activity;
    private FloatingActionMenu fab;
    private GloopUser owner;
    private FragmentManager fragmentManager;
    private UserInfo userInfo;

    public SearchDialog(Activity activity, FloatingActionMenu fab, GloopUser owner, final FragmentManager fragmentManager, UserInfo userInfo) {
        this.activity = activity;
        this.fab = fab;
        this.owner = owner;
        this.fragmentManager = fragmentManager;
        this.userInfo = userInfo;

        show();
    }

    private void show() {
        final View dialogView = View.inflate(activity, R.layout.dialog_search, null);

        final Dialog dialog = new Dialog(activity, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        final EditText tvBoardName = (EditText) dialog.findViewById(R.id.dialog_search_board_name);
        tvBoardName.getBackground().setColorFilter(activity.getResources().getColor(R.color.edit_text_color), PorterDuff.Mode.SRC_IN);


        Button dialogButton = (Button) dialog.findViewById(R.id.dialog_search_btn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String boardName = tvBoardName.getText().toString();

                Board board = Gloop
                        .all(Board.class)
                        .where()
                        .equalsTo("name", boardName)
                        .first();

                if (board != null) {
                    GloopLogger.i("Found board.");

                    // if PUBLIC board add your self to the group.
                    GloopGroup group = Gloop
                            .all(GloopGroup.class)
                            .where()
                            .equalsTo("objectId", board.getOwner())
                            .first();

                    if (group != null) {
                        GloopLogger.i("GloopGroup found add myself to group and save");
                        group.addMember(owner.getUserId());
                        group.save();

                        if (userInfo.getImageURL() != null)
                            board.addMember(userInfo.getEmail(), userInfo.getImageURL().toString());
                        else
                            board.addMember(userInfo.getEmail(), null);

                    } else {
                        GloopLogger.e("GloopGroup not found!");
                    }

                    // save public object to local db.
                    board.save();


                    Context context = v.getContext();
                    Intent intent = new Intent(context, BoardDetailActivity.class);
                    intent.putExtra(BoardDetailFragment.ARG_BOARD, board);
                    intent.putExtra(BoardDetailFragment.ARG_USER_INFO, userInfo);

                    context.startActivity(intent);
                } else {

                    // if the board is not public check the PrivateBoardRequest objects.
                    PrivateBoardRequest privateBoard = Gloop
                            .all(PrivateBoardRequest.class)
                            .where()
                            .equalsTo("boardName", boardName)
                            .first();

                    if (privateBoard != null) {
                        // request access to private board with the BoardAccessRequest object.
                        BoardAccessRequest request = new BoardAccessRequest();
                        request.setUser(privateBoard.getBoardCreator(), PUBLIC | READ | WRITE);
                        request.setBoardName(boardName);
                        request.setBoardCreator(privateBoard.getBoardCreator());
                        request.setUserId(owner.getUserId());
                        request.setBoardGroupId(privateBoard.getGroupId());
                        if (userInfo.getImageURL() != null)
                            request.setUserImageUri(userInfo.getImageURL().toString());
//                        request.setPermission();
                        request.save();
                    } else {
                        GloopLogger.i("Could not find public board with name: " + boardName);
                    }
                }
                dialog.dismiss();
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_search_closeDialogImg);
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

        final View view = dialogView.findViewById(R.id.pop_search);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = fab.getRight() - 100;
        int cy = fab.getBottom() - 500;

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
