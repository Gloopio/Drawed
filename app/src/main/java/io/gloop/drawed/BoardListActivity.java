package io.gloop.drawed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.tr4android.recyclerviewslideitem.SwipeAdapter;
import com.tr4android.recyclerviewslideitem.SwipeConfiguration;

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
    private RecyclerView recyclerView;
    private BoardAdapter boardAdapter;

    private GloopUser owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        recyclerView = (RecyclerView) findViewById(R.id.item_list);

        //set username
        TextView username = (TextView) findViewById(R.id.user_name);

        // Load the currently logged in GloopUser of the app.
        this.owner = Gloop.getOwner();
        if (owner != null) {
            String name = this.owner.getName();
            if (name != null)
                username.setText(name);
        }


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
                new SearchDialog(BoardListActivity.this, owner, mTwoPane, BoardListActivity.this.getSupportFragmentManager(), boardAdapter).show();
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

        FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fab_menu_item_scan);
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchScanner(QRCodeScannerActivity.class);
                floatingActionMenu.close(false);
            }
        });

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Gloop.sync();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
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

    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;


    public void launchScanner(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZBAR_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mClss != null) {
                        Intent intent = new Intent(this, mClss);
//                        intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupRecyclerView();
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                GloopLogger.i(accessRequests);
                for (BoardAccessRequest accessRequest : accessRequests) {
//                    showNotification(accessRequest);
                    new AcceptBoardAccessDialog(BoardListActivity.this, accessRequest);
                }
            }
        });
    }


    private void setupRecyclerView() {
        // Load all locally saved boards to the boards list.
        GloopList<Board> boards = Gloop.allLocal(Board.class);

        boardAdapter = new BoardAdapter(boards);
        recyclerView.setAdapter(boardAdapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public class BoardAdapter extends SwipeAdapter {

        private final GloopList<Board> mValues;
        private final GloopOnChangeListener onChangeListener;


        BoardAdapter(GloopList<Board> boards) {
            mValues = boards;
            // GloopOnChangedListener can be set on GloopLists to get notifications on data changes in the background.
            onChangeListener = new GloopOnChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            };
            mValues.addOnChangeListener(onChangeListener);
        }

        @Override
        public RecyclerView.ViewHolder onCreateSwipeViewHolder(ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, true);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindSwipeViewHolder(RecyclerView.ViewHolder swipeViewHolder, int position) {
            final BoardViewHolder holder = (BoardViewHolder) swipeViewHolder;
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

            int color = board.getColor();

            // check if previous color was the same
            if (position > 0 && mValues.get(position - 1).getColor() == color) {
                holder.mDivider.setBackgroundColor(ColorUtil.darkenColor(color));
                holder.mDivider.setVisibility(View.VISIBLE);
            } else
                holder.mDivider.setVisibility(View.GONE);

            holder.mView.setBackgroundColor(color);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

                    removeOnChangeListener();
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

        public void removeOnChangeListener() {
            mValues.removeOnChangeListener(onChangeListener);
        }

        @Override
        public SwipeConfiguration onCreateSwipeConfiguration(Context context, int position) {
            return new SwipeConfiguration.Builder(context)
                    .setLeftBackgroundColorResource(R.color.color_delete)
                    .setRightBackgroundColorResource(R.color.color_mark)
                    .setDrawableResource(R.drawable.ic_delete_white_24dp)
                    .setRightDrawableResource(R.drawable.ic_settings_white_24dp)
                    // .setLeftUndoable(true)
                    // .setLeftUndoDescription("Undo")
                    .setDescriptionTextColorResource(android.R.color.white)
                    .setLeftSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.NORMAL_SWIPE)
                    .setRightSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                    .build();
        }

        @Override
        public void onSwipe(int position, int direction) {
            Board board = mValues.get(position);

            if (direction == SWIPE_LEFT) {
                board.delete();
            } else {
                new BoardInfoDialog(BoardListActivity.this, owner, board).show();
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class BoardViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            final ImageView mImagePrivate;
            final ImageView mImageFreeze;
            final ImageView mDivider;

            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.list_item);
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