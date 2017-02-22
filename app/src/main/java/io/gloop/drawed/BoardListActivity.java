package io.gloop.drawed;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.utils.ColorUtil;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BoardDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BoardListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    private static final String API_KEY = "TEST";
    private static final boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        new Gloop(this, API_KEY, DEBUG);

        showIntroOnFirstRun();

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Board board = new Board();
                board.setName("Test".toUpperCase());  // TODO set name of board
                board.setColor(ColorUtil.randomColor(getApplicationContext()));
                board.save();

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(BoardDetailFragment.ARG_BOARD, board);
                    BoardDetailFragment fragment = new BoardDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, BoardDetailActivity.class);
                    intent.putExtra(BoardDetailFragment.ARG_BOARD, board);

                    context.startActivity(intent);
                }
            }
        });

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Gloop.sync();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void showIntroOnFirstRun() {
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(BoardListActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        GloopList<Board> boards = Gloop.all(Board.class);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(boards));
    }

    class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final GloopList<Board> mValues;

        SimpleItemRecyclerViewAdapter(GloopList<Board> boards) {
            mValues = boards;
            mValues.addOnChangeListener(new GloopOnChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);

            final Board board = mValues.get(position);

            holder.mContentView.setText(board.getName().toUpperCase());
            if (board.isPrivateBoard())
                holder.mImagePrivate.setVisibility(View.VISIBLE);
            else {
                holder.mImagePrivate.setVisibility(View.GONE);
            }
            if (board.isFreezeBoard())
                holder.mImageFreeze.setVisibility(View.VISIBLE);
            else
                holder.mImageFreeze.setVisibility(View.GONE);

            holder.mView.setBackgroundColor(holder.mItem.getColor());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putSerializable(BoardDetailFragment.ARG_BOARD, holder.mItem);
                        BoardDetailFragment fragment = new BoardDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BoardDetailActivity.class);
                        intent.putExtra(BoardDetailFragment.ARG_BOARD, holder.mItem);

                        context.startActivity(intent);
                    }
                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showStatsPopup(board);
                    return true;
                }
            });
        }

        // opens a dialog on long press on the list item
        private void showStatsPopup(final Board board) {
            final Dialog dialog = new Dialog(BoardListActivity.this, R.style.AppTheme_PopupTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_stats);

            LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.pop_stat_view);
            layout.setBackgroundColor(board.getColor());

            TextView tvBoardName = (TextView) dialog.findViewById(R.id.pop_stat_board_name);
            tvBoardName.setText(board.getName());

            Switch switchPrivate = (Switch) dialog.findViewById(R.id.pop_stat_switch_private);
            switchPrivate.setChecked(board.isPrivateBoard());
            switchPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    board.setPrivateBoard(isChecked);
                    board.save();
                }
            });

            Switch switchFreeze = (Switch) dialog.findViewById(R.id.pop_stat_switch_freeze);
            switchFreeze.setChecked(board.isFreezeBoard());
            switchFreeze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    board.setFreezeBoard(isChecked);
                    board.save();
                }
            });

            Button dialogButton = (Button) dialog.findViewById(R.id.pop_stat_btn_close);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button deleteButton = (Button) dialog.findViewById(R.id.pop_stat_btn_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO impl (right now the board is always deleted, impl to just leave if the user is not the owner)
                    board.delete();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            ImageView mImagePrivate;
            ImageView mImageFreeze;
            Board mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.content);
                mImagePrivate = (ImageView) view.findViewById(R.id.list_item_private_image);
                mImageFreeze = (ImageView) view.findViewById(R.id.list_item_freeze_image);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
