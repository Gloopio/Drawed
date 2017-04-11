package io.gloop.drawed;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopLogger;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.utils.ColorUtil;
import io.gloop.drawed.utils.NameUtil;
import io.gloop.permissions.GloopGroup;

import static io.gloop.permissions.GloopPermission.PUBLIC;
import static io.gloop.permissions.GloopPermission.READ;
import static io.gloop.permissions.GloopPermission.WRITE;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //set username
        TextView username = (TextView) findViewById(R.id.user_name);
        // at the moment name is randomly generated every time the app starts
        String name = Gloop.getOwner().getName();
        if (name != null)
            username.setText(name);

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchPopup();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewBoardPopup(view);
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

    private void showSearchPopup() {
        final Dialog dialog = new Dialog(BoardListActivity.this, R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_search);

        final EditText tvBoardName = (EditText) dialog.findViewById(R.id.pop_search_board_name);


        Button dialogButton = (Button) dialog.findViewById(R.id.pop_search_btn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Board board = Gloop.all(Board.class)
                        .where()
                        .equalsTo("name", tvBoardName.getText().toString())
                        .first();

                if (board != null) {
                    GloopLogger.i("Found board.");

                    // TODO if PUBLIC board add your self to the group.
//                    GloopGroup group = Gloop.all(GloopGroup.class).where().equalsTo("objectId", board.getOwner()).first();
//                    if (group != null) {
//                        group.addMember(Gloop.getOwner().getUserId());
//                        group.save();
//                    }

                    // save public object to local db.
                    board.saveLocal();

                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putSerializable(BoardDetailFragment.ARG_BOARD, board);
                        BoardDetailFragment fragment = new BoardDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BoardDetailActivity.class);
                        intent.putExtra(BoardDetailFragment.ARG_BOARD, board);

                        context.startActivity(intent);
                    }
                } else {
                    GloopLogger.i("Could not find public board with name: " + tvBoardName.getText().toString());


                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showNewBoardPopup(final View view) {
        final Dialog dialog = new Dialog(BoardListActivity.this, R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_new_board);

        final String colorName = NameUtil.randomColor(getApplicationContext());
        final String randomName = NameUtil.randomAdjective(getApplicationContext()) + colorName + NameUtil.randomObject(getApplicationContext());

        final EditText etBoardName = (EditText) dialog.findViewById(R.id.pop_new_board_board_name);
        etBoardName.setText(randomName);


        Button saveButton = (Button) dialog.findViewById(R.id.pop_new_board_btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create and set group by default.
                GloopGroup group = new GloopGroup();
                group.setUser(Gloop.getOwner().getUserId(), PUBLIC | READ | WRITE);
                group.save();

                final Board board = new Board();

                // test to grant additional permission to another user
//                board.addPermission("test", 1000);

                // set name and color
                board.setName(etBoardName.getText().toString());
                board.setColor(ColorUtil.getColorByName(getApplicationContext(), colorName));

                // set board private
                Switch switchPrivate = (Switch) dialog.findViewById(R.id.pop_new_board_switch_private);
                board.setPrivateBoard(switchPrivate.isChecked());

                // set board freeze
                Switch switchFreeze = (Switch) dialog.findViewById(R.id.pop_new_board_switch_freeze);
                board.setFreezeBoard(switchFreeze.isChecked());

                // TODO test all different permissions
                // set permissions depending on the selection.
                if (board.isPrivateBoard()) {
                    if (board.isFreezeBoard())
                        board.setUser(Gloop.getOwner().toString(), READ);
                    else
                        board.setUser(Gloop.getOwner().toString(), READ | WRITE);
                } else if (board.isFreezeBoard()) {
                    if (board.isPrivateBoard())
                        board.setUser(Gloop.getOwner().toString(), READ);
                    else
                        board.setUser(group.getObjectId(), READ | PUBLIC);
                } else {
                    board.setUser(group.getObjectId(), READ | WRITE | PUBLIC);
                }

                // save the created board
                board.save();

                // open the board in detail fragment
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

                // close popup
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        GloopList<Board> boards = Gloop.allLocal(Board.class);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(boards));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final GloopList<Board> mValues;
        private final GloopOnChangeListener onChangeListener;


        SimpleItemRecyclerViewAdapter(GloopList<Board> boards) {
            mValues = boards;
            onChangeListener = new GloopOnChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            };
            GloopLogger.i("On change listener for the board list attached.");
            mValues.addOnChangeListener(onChangeListener);
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

            holder.mContentView.setText(board.getName());
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

                    GloopLogger.i("On change listener for the board list detached.");
                    mValues.removeOnChangeListener(onChangeListener);
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
            final ImageView mImagePrivate;
            final ImageView mImageFreeze;
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
