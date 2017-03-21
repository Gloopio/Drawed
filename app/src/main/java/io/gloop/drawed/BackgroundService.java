package io.gloop.drawed;

import android.app.IntentService;
import android.content.Intent;

import io.gloop.drawed.model.Board;

public class BackgroundService extends IntentService {

    public static final String PARAMETER = "BOARD_TO_SAVE";

    public BackgroundService() {
        super("BackgroundService");
    }

    public BackgroundService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        Board board = (Board) workIntent.getSerializableExtra("BOARD_TO_SAVE");
        board.save();
    }
}