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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.dialogs.BoardInfoDialog;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.UserInfo;
import io.gloop.permissions.GloopUser;
import io.gloop.query.GloopQuery;

public class ListFragment extends Fragment {

    private Context context;
    private GloopUser owner;
    private BoardAdapter boardAdapter;

    RecyclerView recyclerView;
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

        Bundle args = getArguments();
        operation = args.getInt("operation", 0);
        userInfo = (UserInfo) args.getSerializable("userinfo");
        this.owner = (GloopUser) args.getSerializable("owner");

        recyclerView = (RecyclerView) rv.findViewById(R.id.recyclerview);
        setupRecyclerView();

        this.context = getContext();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rv.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Gloop.sync();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupRecyclerView();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
//                        checkForPrivateBoardAccessRequests();
                    }
                }).start();
            }
        });

        return rv;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                // load boards
                GloopList<Board> boards = null;

                if (operation == VIEW_FAVORITES) {
                    GloopQuery<Board> query = Gloop.allLocal(Board.class).where();

                    List<String> favoritesBoardIds = userInfo.getFavoritesBoardId();
                    if (favoritesBoardIds.size() > 0) {
                        for (int i = 0; i < favoritesBoardIds.size() - 1; i++) {
                            query = query.equalsTo("objectId", favoritesBoardIds.get(i)).or();
                        }
                        boards = query.equalsTo("objectId", favoritesBoardIds.get(favoritesBoardIds.size() - 1)).all();
                    } else {
                        boards = Gloop.allLocal(Board.class).where().equalsTo("objectId", "").all(); // empty list
                    }
                } else if (operation == VIEW_MY_BOARDS) {
                    boards = Gloop.allLocal(Board.class);
                } else if (operation == VIEW_BROWSE) {
                    boards = Gloop.all(Board.class);
                }

                boardAdapter = new BoardAdapter(boards);
                // set ui
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
                        recyclerView.setAdapter(boardAdapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

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
        public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BoardViewHolder holder, int position) {
            final Board board = mValues.get(position);

            holder.mContentView.setText(board.getName());
            int color = board.getColor();

            holder.mImage.setBackgroundColor(color);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Context context = view.getContext();
                    Intent intent = new Intent(context, BoardDetailActivity.class);
                    intent.putExtra(BoardDetailFragment.ARG_BOARD, board);

                    context.startActivity(intent);


                    removeOnChangeListener();
                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new BoardInfoDialog(context, owner, board).show();
                    return true;
                }
            });

            if (userInfo.getFavoritesBoardId().contains(board.getObjectId())) {
                holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
                holder.mFavorite.setTag("selected");
            }
            holder.mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((String) holder.mFavorite.getTag()).equals("notSelected")) {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
                        holder.mFavorite.setTag("selected");
                        userInfo.addFavoriteBoardId(board.getObjectId());
                    } else {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_border_black_24dp);
                        holder.mFavorite.setTag("notSelected");
                        userInfo.removeFavoriteBoardId(board.getObjectId());
                    }
                    if (operation == VIEW_FAVORITES) {
                        setupRecyclerView();
                    }
                    userInfo.save();
                }
            });
        }

        void removeOnChangeListener() {
            mValues.removeOnChangeListener(onChangeListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class BoardViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            final ImageView mImage;
            final ImageView mFavorite;


            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mFavorite = (ImageView) view.findViewById(R.id.board_favorite);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
