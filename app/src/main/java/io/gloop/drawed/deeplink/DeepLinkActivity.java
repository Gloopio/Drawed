package io.gloop.drawed.deeplink;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.BoardDetailActivity;
import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.permissions.GloopGroup;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

/**
 * Created by Alex Untertrifaller on 09.05.17.
 */
public class DeepLinkActivity extends Activity {

    public static final String BASE_DEEP_LINK = "app://drawed.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() >= 1) {
                String parameter1 = segments.get(0);
                showPopup(parameter1);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    // opens a dialog on long press on the list item
    private void showPopup(final String boardName) {
        final Dialog dialog = new Dialog(this, R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_invire_request);

        TextView tvBoardName = (TextView) dialog.findViewById(R.id.dialog_invite_request_text);
        tvBoardName.setText(String.format("Do you want to access the board %s?", boardName));

        Button buttonDeny = (Button) dialog.findViewById(R.id.dialog_invite_request_deny);
        buttonDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                DeepLinkActivity.this.finish();
            }
        });

        Button acceptButton = (Button) dialog.findViewById(R.id.dialog_invite_request_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Gloop(DeepLinkActivity.this);

                if (Gloop.loginWithRememberedUser()) {
                    Board board = Gloop.all(Board.class).where().equalsTo("name", boardName).first();
                    if (board != null) {
                        if (!board.isPrivateBoard()) {
                            GloopGroup group = Gloop
                                    .all(GloopGroup.class)
                                    .where()
                                    .equalsTo("objectId", board.getOwner())
                                    .first();

                            if (group != null) {
                                GloopLogger.i("GloopGroup found add myself to group and save");
                                group.addMember(Gloop.getOwner().getUserId());
                                group.save();
                            } else {
                                GloopLogger.e("GloopGroup not found!");
                            }
                            board.save();
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
                                request.setUserId(Gloop.getOwner().getUserId());
                                request.setBoardGroupId(privateBoard.getGroupId());
                                request.save();
                            } else {
                                GloopLogger.i("Could not find public board with name: " + boardName);
                            }
                        }

                        Intent intent = new Intent(getApplicationContext(), BoardDetailActivity.class);
                        intent.putExtra(BoardDetailFragment.ARG_BOARD, board);
                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), "Board added to your list.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not find the board.", Toast.LENGTH_LONG).show();

                    }
                }
                dialog.dismiss();
                DeepLinkActivity.this.finish();
            }
        });

        dialog.show();
    }
}