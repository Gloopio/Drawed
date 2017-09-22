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

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.dialogs.BoardInfoDialog;
import io.gloop.drawed.model.Board;
import io.gloop.permissions.GloopUser;

public class ListFragment extends Fragment {

    private Context context;
    private GloopUser owner;
    private BoardAdapter boardAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rv = (RelativeLayout) inflater.inflate(
                R.layout.fragment_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) rv.findViewById(R.id.recyclerview);
        setupRecyclerView(recyclerView);

        this.context = getContext();

        // Load the currently logged in GloopUser of the app.
        this.owner = Gloop.getOwner();

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) rv.findViewById(R.id.swipe_refresh_layout);
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
                                setupRecyclerView(recyclerView);
                            }
                        });
//                        checkForPrivateBoardAccessRequests(); TODO
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });

        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        GloopList<Board> boards = Gloop.allLocal(Board.class);

        boardAdapter = new BoardAdapter(boards);
        recyclerView.setAdapter(boardAdapter);
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
//            if (board.isPrivateBoard())
//                holder.mImagePrivate.setVisibility(View.VISIBLE);
//            else
//                holder.mImagePrivate.setVisibility(View.GONE);
//
//            if (board.isFreezeBoard())
//                holder.mImageFreeze.setVisibility(View.VISIBLE);
//            else
//                holder.mImageFreeze.setVisibility(View.GONE);
//
            int color = board.getColor();
//
//            // check if previous color was the same
//            if (position > 0 && mValues.get(position - 1).getColor() == color) {
//                holder.mDivider.setBackgroundColor(ColorUtil.darkenColor(color));
//                holder.mDivider.setVisibility(View.VISIBLE);
//            } else
//                holder.mDivider.setVisibility(View.GONE);

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

            holder.mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO
                    if (((String) holder.mFavorite.getTag()).equals("notSelected")) {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
                        holder.mFavorite.setTag("selected");
                    } else {
                        holder.mFavorite.setImageResource(R.drawable.ic_star_border_black_24dp);
                        holder.mFavorite.setTag("notSelected");
                    }
                }
            });
        }

        public void removeOnChangeListener() {
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
//            final ImageView mImagePrivate;
//            final ImageView mImageFreeze;
//            final ImageView mDivider;

            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mFavorite = (ImageView) view.findViewById(R.id.board_favorite);
//                mImagePrivate = (ImageView) view.findViewById(R.id.list_item_private_image);
//                mImageFreeze = (ImageView) view.findViewById(R.id.list_item_freeze_image);
//                mDivider = (ImageView) view.findViewById(R.id.list_item_divider);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
