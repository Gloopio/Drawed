package io.gloop.drawed.deeplink;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import io.gloop.drawed.model.BoardInfo;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.drawed.model.UserInfo;
import io.gloop.drawed.utils.SharedPreferencesStore;
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

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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

        final Dialog dialog = new Dialog(DeepLinkActivity.this, R.style.AppTheme_PopupTheme);
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

        final Context context = getApplicationContext();

        Button acceptButton = (Button) dialog.findViewById(R.id.dialog_invite_request_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                new AsyncTask<Void, Void, Board>() {

                    private ProgressDialog progress;
                    private UserInfo userInfo;
                    private String errorMessage;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progress = new ProgressDialog(DeepLinkActivity.this);
                        progress.setTitle("Loading");
                        progress.setMessage("Wait while loading lines.");
                        progress.setCancelable(false);
                        progress.show();
                    }

                    @Override
                    protected Board doInBackground(Void... voids) {
                        SharedPreferencesStore.setContext(context);

                        Gloop.initialize(DeepLinkActivity.this);

                        if (Gloop.login(SharedPreferencesStore.getEmail(), SharedPreferencesStore.getPassword())) {

                            userInfo = Gloop.allLocal(UserInfo.class)
                                    .where()
                                    .equalsTo("email", Gloop.getOwner().getName())
                                    .first();

                            BoardInfo boardInfo = Gloop.all(BoardInfo.class).where().equalsTo("name", boardName).first();
                            if (boardInfo != null && !boardInfo.isPrivateBoard()) {
                                GloopGroup group = Gloop
                                        .all(GloopGroup.class)
                                        .where()
                                        .equalsTo("objectId", boardInfo.getOwner())
                                        .first();

                                if (group != null) {
                                    GloopLogger.i("GloopGroup found add myself to group and save");
                                    group.addMember(Gloop.getOwner().getUserId());
                                    group.save();
                                } else {
                                    GloopLogger.e("GloopGroup not found!");
                                }
                                boardInfo.save();
                                return Gloop.all(Board.class).where().equalsTo("objectId", boardInfo.getBoardId()).first();
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
                                    if (userInfo.getImageURL() != null)
                                        request.setUserImageUri(userInfo.getImageURL().toString());
                                    request.save();
                                } else {
                                    GloopLogger.i("Could not find public board with name: " + boardName);
                                }
                            }
                            errorMessage = "Request to access private board is send.";
                            return null;

                        } else {
                            errorMessage = "Could not find the board.";
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Board board) {
                        super.onPostExecute(board);

                        if (board != null) {
                            Intent intent = new Intent(getApplicationContext(), BoardDetailActivity.class);
                            intent.putExtra(BoardDetailFragment.ARG_BOARD, board);
                            intent.putExtra(BoardDetailFragment.ARG_USER_INFO, userInfo);
                            startActivity(intent);

                            Toast.makeText(getApplicationContext(), "Board added to your list.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }

                        progress.dismiss();

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
                        dialog.dismiss();
                        DeepLinkActivity.this.finish();
//                            }
//                        });
                    }
                }.execute();


            }
        });

        dialog.show();

    }
}