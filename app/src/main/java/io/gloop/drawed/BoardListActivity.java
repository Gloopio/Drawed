package io.gloop.drawed;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import io.gloop.drawed.deeplink.DeepLinkActivity;
import io.gloop.drawed.dialogs.AcceptBoardAccessDialog;
import io.gloop.drawed.dialogs.NewBoardDialog;
import io.gloop.drawed.dialogs.SearchDialog;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.recivers.NotificationReceiver;
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
        setContentView(R.layout.activity_item_list);
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
                SearchDialog.show(BoardListActivity.this ,owner, mTwoPane, BoardListActivity.this.getSupportFragmentManager());
                floatingActionMenu.close(false);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewBoardDialog.show(BoardListActivity.this ,owner, view, mTwoPane, BoardListActivity.this.getSupportFragmentManager());
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


    private void share(String username, Board board) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = username + " want'ss to share the board " + board.getName() + " with you. " + DeepLinkActivity.BASE_DEEP_LINK + board.getName();
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
                    AcceptBoardAccessDialog.show(BoardListActivity.this, accessRequest);
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

            Button shareButton = (Button) dialog.findViewById(R.id.pop_stat_btn_share);
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