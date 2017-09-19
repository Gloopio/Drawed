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
        RelativeLayout rv = (RelativeLayout) inflater.inflate(
                R.layout.fragment_list, container, false);
        setupRecyclerView((RecyclerView) rv.findViewById(R.id.recyclerview));

        this.context = getContext();

        // Load the currently logged in GloopUser of the app.
        this.owner = Gloop.getOwner();
        if (owner != null) {
//            String name = this.owner.getName();
//            if (name != null)
//                username.setText(name);   TODO
        }

        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        GloopList<Board> boards = Gloop.allLocal(Board.class);

        boardAdapter = new BoardAdapter(boards);
        recyclerView.setAdapter(boardAdapter);
    }

//    public static class SimpleStringRecyclerViewAdapter
//            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {
//
//        private final TypedValue mTypedValue = new TypedValue();
//        private int mBackground;
//        private List<String> mValues;
//
//        public static class ViewHolder extends RecyclerView.ViewHolder {
//            public String mBoundString;
//
//            public final View mView;
//            public final ImageView mImageView;
//            public final TextView mTextView;
//
//            public ViewHolder(View view) {
//                super(view);
//                mView = view;
//                mImageView = (ImageView) view.findViewById(R.id.avatar);
//                mTextView = (TextView) view.findViewById(android.R.id.text1);
//            }
//
//            @Override
//            public String toString() {
//                return super.toString() + " '" + mTextView.getText();
//            }
//        }
//
//        public SimpleStringRecyclerViewAdapter(Context context, List<String> items) {
//            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//            mBackground = mTypedValue.resourceId;
//            mValues = items;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.list_item, parent, false);
//            view.setBackgroundResource(mBackground);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            holder.mBoundString = mValues.get(position);
//            holder.mTextView.setText(mValues.get(position));
//
//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, BoardDetailActivity.class);
//                    intent.putExtra(BoardDetailFragment.ARG_BOARD, board);
//
//                    context.startActivity(intent);
//                }
//            });
//
//            Glide.with(holder.mImageView.getContext())
//                    .load(R.drawable.cheese_1)
//                    .fitCenter()
//                    .into(holder.mImageView);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mValues.size();
//        }
//
//
//    }

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
        public void onBindViewHolder(BoardViewHolder holder, int position) {
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
//            final ImageView mImagePrivate;
//            final ImageView mImageFreeze;
//            final ImageView mDivider;

            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mImage = (ImageView) view.findViewById(R.id.avatar);
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
