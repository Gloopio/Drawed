package io.gloop.drawed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopLogger;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.dialogs.AcceptBoardAccessDialog;
import io.gloop.drawed.dialogs.BoardInfoDialog;
import io.gloop.drawed.dialogs.NewBoardDialog;
import io.gloop.drawed.dialogs.SearchDialog;
import io.gloop.drawed.dialogs.UserDialog;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.utils.ColorUtil;
import io.gloop.drawed.utils.NotificationUtil;
import io.gloop.permissions.GloopUser;

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

    private GloopUser owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //set username
        TextView username = (TextView) findViewById(R.id.user_name);
        // at the moment name is randomly generated every time the app starts
        this.owner = Gloop.getOwner();
        String name = this.owner.getName();
        if (name != null)
            username.setText(name);


        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        final FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fab_menu_item_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchDialog(BoardListActivity.this, owner, mTwoPane, BoardListActivity.this.getSupportFragmentManager()).show();
                floatingActionMenu.close(false);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NewBoardDialog(BoardListActivity.this, owner, view, mTwoPane, BoardListActivity.this.getSupportFragmentManager()).show();
                floatingActionMenu.close(false);
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

        LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserDialog(BoardListActivity.this, owner).show();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setupRecyclerView();
                checkForPrivateBoardAccessRequests();
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();

        setupRecyclerView();
        checkForPrivateBoardAccessRequests();
    }

    @Override
    public void onStop() {
        super.onStop();
        SaveInBackgroundWorker.getInstance().stopWorker();
    }

    @Override
    public void onPause() {
        super.onPause();
        SaveInBackgroundWorker.getInstance().stopWorker();
    }


    private void checkForPrivateBoardAccessRequests() {
        final GloopList<BoardAccessRequest> accessRequests = Gloop
                .all(BoardAccessRequest.class)
                .where()
                .equalsTo("boardCreator", owner.getUserId())
                .all();
        for (BoardAccessRequest accessRequest : accessRequests) {
            NotificationUtil.show(BoardListActivity.this, accessRequest);
        }

        accessRequests.addOnChangeListener(new GloopOnChangeListener() {
            @Override
            public void onChange() {
                GloopLogger.i("Request access to a private board");
//                GloopList<BoardAccessRequest> accessRequests = Gloop
//                        .allLocal(BoardAccessRequest.class)
//                        .where()
//                        .equalsTo("boardCreator", Gloop.getOwner().getUserId())
//                        .all();
                GloopLogger.i(accessRequests);
                for (BoardAccessRequest accessRequest : accessRequests) {
//                    showNotification(accessRequest);
                    new AcceptBoardAccessDialog(BoardListActivity.this, accessRequest);
                }
            }
        });
    }


    private void setupRecyclerView() {
        GloopList<Board> boards = Gloop.allLocal(Board.class);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.item_list);
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
            else
                holder.mImagePrivate.setVisibility(View.GONE);

            if (board.isFreezeBoard())
                holder.mImageFreeze.setVisibility(View.VISIBLE);
            else
                holder.mImageFreeze.setVisibility(View.GONE);

            int color = holder.mItem.getColor();

            // check if previous color was the same
            if (position > 0 && mValues.get(position - 1).getColor() == color) {
                holder.mDivider.setBackgroundColor(ColorUtil.darkenColor(color));
                holder.mDivider.setVisibility(View.VISIBLE);
            } else
                holder.mDivider.setVisibility(View.GONE);

            holder.mView.setBackgroundColor(color);

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

                    mValues.removeOnChangeListener(onChangeListener);
                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new BoardInfoDialog(BoardListActivity.this, owner, board).show();
                    return true;
                }
            });
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
            final ImageView mDivider;
            Board mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.content);
                mImagePrivate = (ImageView) view.findViewById(R.id.list_item_private_image);
                mImageFreeze = (ImageView) view.findViewById(R.id.list_item_freeze_image);
                mDivider = (ImageView) view.findViewById(R.id.list_item_divider);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}