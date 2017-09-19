package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.BoardDetailActivity;
import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopUser;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class SearchDialog extends Dialog {

    public SearchDialog(final @NonNull Context context, final GloopUser owner, final boolean mTwoPane, final FragmentManager fragmentManager) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search);

        final EditText tvBoardName = (EditText) findViewById(R.id.dialog_search_board_name);


        Button dialogButton = (Button) findViewById(R.id.dialog_search_btn);
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
                    } else {
                        GloopLogger.e("GloopGroup not found!");
                    }

                    // save public object to local db.
                    board.save();

                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putSerializable(BoardDetailFragment.ARG_BOARD, board);
                        BoardDetailFragment fragment = new BoardDetailFragment();
                        fragment.setArguments(arguments);
                        fragmentManager.beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BoardDetailActivity.class);
                        intent.putExtra(BoardDetailFragment.ARG_BOARD, board);

                        context.startActivity(intent);
                    }
//                    adapter.removeOnChangeListener();
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
                        request.save();
                    } else {
                        GloopLogger.i("Could not find public board with name: " + boardName);
                    }
                }
                dismiss();
            }
        });
    }
}
