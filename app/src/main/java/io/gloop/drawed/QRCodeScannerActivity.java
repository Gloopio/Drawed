package io.gloop.drawed;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.List;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.BoardInfo;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.drawed.model.UserInfo;
import io.gloop.permissions.GloopGroup;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

public class QRCodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void handleResult(Result rawResult) {
        String contents = rawResult.getText();
        Uri uri = Uri.parse(contents);

        final List<String> segments = uri.getPathSegments();
        if (segments.size() >= 1) {
            String boardName = segments.get(0);

            BoardInfo boardInfo = Gloop.all(BoardInfo.class)
                    .where()
                    .equalsTo("name", boardName)
                    .first();

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

                UserInfo userInfo = Gloop.allLocal(UserInfo.class)
                        .where()
                        .equalsTo("email", Gloop.getOwner().getName())
                        .first();

                Intent intent = new Intent(getApplicationContext(), BoardDetailActivity.class);
                intent.putExtra(BoardDetailFragment.ARG_BOARD,  Gloop.all(Board.class).where().equalsTo("objectId", boardInfo.getBoardId()).first());
                intent.putExtra(BoardDetailFragment.ARG_USER_INFO, userInfo);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), R.string.board_added, Toast.LENGTH_LONG).show();
            } else {
                // if the board is not public check the PrivateBoardRequest objects.
                PrivateBoardRequest privateBoard = Gloop
                        .all(PrivateBoardRequest.class)
                        .where()
                        .equalsTo("boardName", boardName)
                        .first();

                if (privateBoard != null) {

                    UserInfo userInfo = Gloop.allLocal(UserInfo.class)
                            .where()
                            .equalsTo("email", Gloop.getOwner().getName())
                            .first();


                    BoardAccessRequest o = Gloop.all(BoardAccessRequest.class).where()
                            .equalsTo("boardName", boardName)
                            .and()
                            .equalsTo("userId", Gloop.getOwner().getUserId())
                            .first();
                    if (o == null) {

                        // request access to private board with the BoardAccessRequest object.
                        BoardAccessRequest request = new BoardAccessRequest();
                        request.setUser(privateBoard.getBoardCreator(), PUBLIC | READ | WRITE);
                        request.setBoardName(boardName);
                        request.setBoardCreator(privateBoard.getBoardCreator());
                        request.setUserId(Gloop.getOwner().getUserId());
                        request.setBoardGroupId(privateBoard.getGroupId());
                        if (userInfo != null)
                            request.setUserImageUri(userInfo.getImageURL().toString());
                        request.save();
                        Toast.makeText(getApplicationContext(), R.string.request_access_to_board, Toast.LENGTH_LONG).show();
                    }
                    mScannerView.stopCamera();
                    finish();
//                    mScannerView.resumeCameraPreview(QRCodeScannerActivity.this);
                } else {
                    GloopLogger.i("Could not find public board with name: " + boardName);
                }

            }
        }
    }
}