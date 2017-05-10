package io.gloop.drawed;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopLogger;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.PrivateBoardRequest;
import io.gloop.drawed.utils.ColorUtil;
import io.gloop.drawed.utils.NameUtil;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopUser;

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

    private GloopUser owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //set username
        TextView username = (TextView) findViewById(R.id.user_name);
        // at the moment name is randomly generated every time the app starts
        this.owner = Gloop.getOwner();
        String name = this.owner.getName();
        if (name != null)
            username.setText(name);


        setupRecyclerView();

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
                showSearchPopup();
                floatingActionMenu.close(false);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewBoardPopup(view);
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

        checkForPrivateBoardAccessRequests();
    }

    @Override
    public void onResume() {
        super.onResume();

        setupRecyclerView();
        checkForPrivateBoardAccessRequests();
    }

    private void share(String username, Board board) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        // Todo change deep link to look nicer
        String shareBody = username + " want's to share the board " + board.getName() + " with you. drawed://gloop.io/methodDeepLink/" + board.getName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Drawed Board Invite");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void checkForPrivateBoardAccessRequests() {
        final GloopList<BoardAccessRequest> accessRequests = Gloop
                .all(BoardAccessRequest.class)
                .where()
                .equalsTo("boardCreator", owner.getUserId())
                .all();
        for (BoardAccessRequest accessRequest : accessRequests) {
            showNotification(accessRequest);
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
                    showAcceptAccessToBoardPopup(accessRequest);
                }
            }
        });
    }

    private void showNotification(BoardAccessRequest accessRequest) {
        GloopLogger.i("Grant access to user via notification");

        Context ctx = getApplicationContext();

        Intent intent = new Intent(ctx, SplashActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Drawed")
                .setContentTitle("Grant user access to private board")
                .setContentText("Give user: " + accessRequest.getUserId() + " access to board: " + accessRequest.getBoardName())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");

        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction(NotificationReceiver.YES_ACTION);
        yesReceive.putExtra(NotificationReceiver.ACCESS_REQUEST, accessRequest);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        b.addAction(R.drawable.ic_done_black_24dp, "Yes", pendingIntentYes);

        //No intent
        Intent noReceive = new Intent();
        noReceive.setAction(NotificationReceiver.NO_ACTION);
        yesReceive.putExtra(NotificationReceiver.ACCESS_REQUEST, accessRequest);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 12345, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        b.addAction(R.drawable.ic_clear_black_24dp, "No", pendingIntentNo);


        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
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

                GloopGroup group = new GloopGroup();
                group.setUser(owner.getUserId(), PUBLIC | READ | WRITE);

                // set permissions depending on the selection.
                if (board.isPrivateBoard()) {
                    group.setUser(owner.getUserId(), READ | WRITE);
                    if (board.isFreezeBoard())
                        board.setUser(group.getObjectId(), READ);
                    else
                        board.setUser(group.getObjectId(), READ | WRITE);
                } else if (board.isFreezeBoard()) {
                    if (board.isPrivateBoard()) {
                        group.setUser(owner.getUserId(), READ | WRITE);
                        board.setUser(group.getObjectId(), READ);
                    } else
                        board.setUser(group.getObjectId(), READ | PUBLIC);
                } else {
                    board.setUser(group.getObjectId(), READ | WRITE | PUBLIC);
                }

                group.save();

                if (board.isPrivateBoard()) {
                    // this is used to discover private boards and request access to it.
                    PrivateBoardRequest privateBoard = new PrivateBoardRequest();
                    privateBoard.setUser(board.getOwner(), READ | WRITE | PUBLIC);
                    privateBoard.setBoardName(board.getName());
                    privateBoard.setBoardCreator(owner.getUserId());
                    privateBoard.setGroupId(group.getObjectId());
                    privateBoard.save();
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

    private void showAcceptAccessToBoardPopup(final BoardAccessRequest request) {
        GloopLogger.i("Show access user popup.");
        final Dialog dialog = new Dialog(BoardListActivity.this, R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_acceped_board_access);

        TextView textView = (TextView) dialog.findViewById(R.id.pop_accept_text);
        textView.setText("Allow access to user " + request.getUserId() + " on board " + request.getBoardName());

        //grant access
        Button grantButton = (Button) dialog.findViewById(R.id.pop_accept_btn_grant);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GloopGroup group = Gloop
                        .all(GloopGroup.class)
                        .where()
                        .equalsTo("objectId", request.getBoardGroupId())
                        .first();
                group.addMember(request.getUserId());
                group.save();

                request.delete();

                dialog.dismiss();
            }
        });
        // deny access
        Button denyButton = (Button) dialog.findViewById(R.id.pop_accept_btn_deny);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.delete();
                dialog.dismiss();
            }
        });
        dialog.show();
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
                    board.saveInBackground();
                }
            });

            Switch switchFreeze = (Switch) dialog.findViewById(R.id.pop_stat_switch_freeze);
            switchFreeze.setChecked(board.isFreezeBoard());
            switchFreeze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    board.setFreezeBoard(isChecked);
                    board.saveInBackground();
                }
            });

            Button shareButton =(Button) dialog.findViewById(R.id.pop_stat_btn_share);
//            if (owner.getName().equals(board.getGloopUser()))
//                shareButton.setVisibility(View.VISIBLE);
//            else
//                shareButton.setVisibility(View.GONE);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    share(owner.getName(), board);
                    dialog.dismiss();
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
                    if (!owner.getName().equals(board.getGloopUser()))
                        board.deleteLocal();
                    else
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
