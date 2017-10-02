package io.gloop.drawed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;

public class BoardDetailActivity extends AppCompatActivity {

    // Goro instance
//    private BoundGoro goro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // run activity in fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putSerializable(BoardDetailFragment.ARG_BOARD, getIntent().getSerializableExtra(BoardDetailFragment.ARG_BOARD));
            BoardDetailFragment fragment = new BoardDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }

//        goro = Goro.bindWith(this, new BoundGoro.OnUnexpectedDisconnection() {
//            @Override
//            public void onServiceDisconnected(BoundGoro goro) {
//                GloopLogger.e(goro);
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, BoardListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    public void schedule(Callable submissionTask) {
//        goro.schedule(submissionTask);
//    }
//
//    protected void onStart() {
//        super.onStart();
//        goro.bind();
//    }
//
//    protected void onStop() {
//        super.onStop();
//        goro.unbind();
//    }


}
