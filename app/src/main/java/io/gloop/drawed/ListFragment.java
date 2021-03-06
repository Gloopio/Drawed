/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gloop.drawed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.constants.Constants;
import io.gloop.drawed.dialogs.AcceptBoardAccessDialog;
import io.gloop.drawed.dialogs.BoardInfoDialog;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.BoardInfo;
import io.gloop.drawed.model.UserInfo;
import io.gloop.exceptions.GloopException;
import io.gloop.exceptions.GloopLoadException;
import io.gloop.permissions.GloopUser;
import io.gloop.query.GloopQuery;

public class ListFragment extends Fragment {

    private final static String SELECTED = "selected";
    private final static String NOT_SELECTED = "notSelected";

    private Context context;
    private GloopUser owner;
    private BoardAdapter boardAdapter;

    private TextView infoText;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static final int VIEW_FAVORITES = 0;
    public static final int VIEW_MY_BOARDS = 1;
    public static final int VIEW_BROWSE = 2;

    private int operation;
    private UserInfo userInfo;

    public static ListFragment newInstance(int operation, UserInfo userinfo, GloopUser owner) {
        ListFragment f = new ListFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("operation", operation);
        args.putSerializable("userinfo", userinfo);
        args.putSerializable("owner", owner);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rv = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        operation = args.getInt("operation", 0);
        userInfo = (UserInfo) args.getSerializable("userinfo");
        this.owner = (GloopUser) args.getSerializable("owner");

        infoText = (TextView) rv.findViewById(R.id.fragment_list_info_text);

        recyclerView = (RecyclerView) rv.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    ((BoardListActivity) getActivity()).setFABVisibility(View.INVISIBLE);
                } else {
                    ((BoardListActivity) getActivity()).setFABVisibility(View.VISIBLE);
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        this.context = getContext();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rv.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();

            }
        });

        return rv;
    }

    private boolean running = false;

    private void update() {
        if (!running)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    running = true;
                    Gloop.sync();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupRecyclerView();
                            checkForPrivateBoardAccessRequests();
                            running = false;
                        }
                    });

                }
            }).start();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
        checkForPrivateBoardAccessRequests();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                boardAdapter.filter(s);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                update();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkForPrivateBoardAccessRequests() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final GloopList<BoardAccessRequest> accessRequests = Gloop
                            .all(BoardAccessRequest.class)
                            .where()
                            .equalsTo("boardCreator", owner.getUserId())
                            .all();
                    if (accessRequests != null) {
                        for (final BoardAccessRequest accessRequest : accessRequests) {
                            final FragmentActivity activity = getActivity();
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            new AcceptBoardAccessDialog(activity, accessRequest).show();
                                        }
                                    }
                            );
                        }
                    }
                } catch (GloopException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class LoadBoardsTask extends AsyncTask<Void, Integer, GloopList<BoardInfo>> {

        private String info = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected GloopList<BoardInfo> doInBackground(Void... urls) {
            // load boardInfos
            GloopList<BoardInfo> boardInfos = null;


            if (operation == VIEW_FAVORITES) {
                GloopQuery<BoardInfo> query = Gloop.allLocal(BoardInfo.class).where();

                List<String> favoritesBoardIds = userInfo.getFavoritesBoardId();
                if (favoritesBoardIds.size() > 0) {
                    for (int i = 0; i < favoritesBoardIds.size() - 1; i++) {
                        query = query.equalsTo("boardId", favoritesBoardIds.get(i)).or();
                    }
                    boardInfos = query.equalsTo("boardId", favoritesBoardIds.get(favoritesBoardIds.size() - 1)).all();
                } else {
                    info = "No favourites\n" +
                            "Favourite an board by tapping the star next to it.";
                    boardInfos = Gloop.allLocal(BoardInfo.class).where().equalsTo("objectId", "").all(); // empty list
                }

            } else if (operation == VIEW_MY_BOARDS) {
                boardInfos = Gloop.allLocal(BoardInfo.class);
                if (boardInfos.size() == 0) {
                    info = "No Boards\n" +
                            "Create a new board using the ‘+’ below.";
                }
            } else if (operation == VIEW_BROWSE) {
                boardInfos = Gloop.all(BoardInfo.class);
                boardInfos.load();
            }

            return boardInfos;
        }


        @Override
        protected void onPostExecute(GloopList<BoardInfo> boardInfos) {
            super.onPostExecute(boardInfos);
            try {
                boardAdapter = new BoardAdapter(boardInfos);
                recyclerView.setAdapter(boardAdapter);
                mSwipeRefreshLayout.setRefreshing(false);

                if (info != null) {
                    infoText.setText(info);
                } else {
                    infoText.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupRecyclerView() {
        new LoadBoardsTask().execute();
    }

    public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

        private ArrayList<BoardInfo> list;
        private final GloopList<BoardInfo> originalList;

        BoardAdapter(final GloopList<BoardInfo> boards) {
            originalList = boards;
            if (boards.size() != 0) {
                list = (ArrayList<BoardInfo>) boards.getLocalCopy();
                Collections.sort(list, Collections.reverseOrder(new Comparator<BoardInfo>() {
                    @Override
                    public int compare(BoardInfo left, BoardInfo right) {
                        return Long.compare(left.getTimestamp(), right.getTimestamp());
                    }
                }));
            }

//            originalList.addOnChangeListener(new GloopOnChangeListener() {
//                @Override
//                public void onChange() {
//                    list = (ArrayList<BoardInfo>) boards.getLocalCopy();
//                    notifyDataSetChanged();
//                }
//            });
        }

        @Override
        public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BoardViewHolder holder, int position) {
            final BoardInfo boardInfo = list.get(position);

            holder.mContentView.setText(boardInfo.getName());
            final int color = boardInfo.getColor();

            holder.mImage.setBackgroundColor(color);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    holder.mView.setClickable(false);
                    holder.mView.setEnabled(false);

                    new AsyncTask<Void, Void, Board>() {

                        private ProgressDialog progress;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            progress = new ProgressDialog(context);
                            progress.setTitle(getString(R.string.loading));
                            progress.setMessage(getString(R.string.wait_while_loading_lines));
                            progress.setCancelable(false);
                            progress.show();
                        }

                        @Override
                        protected Board doInBackground(Void... voids) {
                            return Gloop.all(Board.class)
                                    .where()
                                    .equalsTo(Constants.OBJECT_ID, boardInfo.getBoardId())
                                    .first();
                        }

                        @Override
                        protected void onPostExecute(Board board) {
                            super.onPostExecute(board);
                            Context context = view.getContext();
                            Intent intent = new Intent(context, BoardDetailActivity.class);
                            intent.putExtra(BoardDetailFragment.ARG_BOARD, board);
                            intent.putExtra(BoardDetailFragment.ARG_USER_INFO, userInfo);

                            context.startActivity(intent);

                            progress.dismiss();
                            holder.mView.setClickable(true);
                            holder.mView.setEnabled(true);
                        }
                    }.execute();
                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new BoardInfoDialog(context, owner, boardInfo, userInfo, 100.0, 100.0);
                    setupRecyclerView();
                    return true;
                }
            });

            if (userInfo.getFavoritesBoardId().contains(boardInfo.getBoardId())) {
                holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
                holder.mFavorite.setTag(SELECTED);
            }
            holder.mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.mFavorite.getTag().equals(NOT_SELECTED)) {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
                        holder.mFavorite.setTag(SELECTED);
                        userInfo.addFavoriteBoardId(boardInfo.getBoardId());
                    } else {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_border_black_24dp);
                        holder.mFavorite.setTag(NOT_SELECTED);
                        userInfo.removeFavoriteBoardId(boardInfo.getBoardId());
                    }
                    if (operation == VIEW_FAVORITES) {
                        setupRecyclerView();
                    }
                    userInfo.saveInBackground();
                }
            });

            try {
                holder.mLines.setText(getString(R.string.line_size, boardInfo.getSize()));
            } catch (Exception ignore) {

            }

            setMemberImages(boardInfo, holder);
        }

        private void setMemberImages(BoardInfo board, BoardViewHolder holder) {
            int count = 0;
            for (Map.Entry<String, String> entry : board.getMembers().entrySet()) {
                if (entry.getValue() != null)
                    Picasso.with(context)
                            .load(Uri.parse(entry.getValue()))
                            .into(holder.memberImages.get(count++));
                else {
                    holder.memberImages.get(count++).setImageResource(R.drawable.user_with_background);
                }
                if (count >= 4)
                    break;
            }
            if (count < 4) {
                for (int i = count; i < 4; i++) {
                    holder.memberImages.get(i).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            try {
                if (list != null)
                    return list.size();
                else
                    return 0;
            } catch (GloopLoadException e) {
//                e.printStackTrace();
                return 0;
            }
        }

        void filter(String s) {
            if (s.equals("")) {
                list = (ArrayList<BoardInfo>) originalList.getLocalCopy();
            } else {
                String search = s.toLowerCase();
                for (BoardInfo boardInfo : originalList) {
                    if (!boardInfo.getName().toLowerCase().startsWith(search))
                        list.remove(boardInfo);
                }
            }

            notifyDataSetChanged();
        }

        class BoardViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            final TextView mLines;
            final ImageView mImage;
            final ImageView mFavorite;

            final List<CircleImageView> memberImages = new ArrayList<>();


            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mLines = (TextView) view.findViewById(R.id.lines);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mFavorite = (ImageView) view.findViewById(R.id.board_favorite);

                memberImages.add((CircleImageView) view.findViewById(R.id.user_image1));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image2));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image3));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image4));
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
