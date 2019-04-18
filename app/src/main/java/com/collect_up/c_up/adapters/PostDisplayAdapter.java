/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.binders.DisplayPostBinder;
import com.collect_up.c_up.adapters.binders.PostCommentsBinder;
import com.collect_up.c_up.fragments.FragmentPostDisplay;
import com.collect_up.c_up.model.Comment;
import com.collect_up.c_up.model.Post;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PostDisplayAdapter extends UltimateDifferentViewTypeAdapter {

  private final FragmentPostDisplay mfragmentPostDisplay;
  public PostCommentsBinder mPostCommentsBinder;
  public List<Comment> mComments = new ArrayList<>();
  private DisplayPostBinder mDisplayPostBinder;
  private Post mPost;

  public PostDisplayAdapter(FragmentPostDisplay fragmentPostDisplay, Post post) {
    mfragmentPostDisplay = fragmentPostDisplay;

    mPost = post;

    putPostBinder();
    putCommentsBinder();
    notifyDataSetChanged();
  }

  public void putPostBinder() {
    mDisplayPostBinder = new DisplayPostBinder(this, mPost, mfragmentPostDisplay.getContext());
    putBinder(Types.TYPE_POST, mDisplayPostBinder);
  }

  public void putCommentsBinder() {
    mPostCommentsBinder = new PostCommentsBinder(this, mComments, mPost, mfragmentPostDisplay);
    putBinder(Types.TYPE_COMMENT, mPostCommentsBinder);
  }

  public void notifyPostBinder(Post post) {
    mPost = post;
    notifyBinderItemChanged(mDisplayPostBinder, 0);
  }

  public void removeNullItem() {
    for (Comment comment : mComments)
    {
      if (comment == null)
      {
        removeInternal(mComments, mComments.indexOf(null));
        break;
      }
    }
  }

  public void updatePost(Post post) {
    mPost = post;
    mDisplayPostBinder.updatePost(mPost);
    mPostCommentsBinder.updatePost(mPost);

    notifyDataSetChanged();
  }

  public void addComments(List<Comment> comments) {
    for (Comment comment : comments)
    {
      insertInternal(mComments, comment, mComments.size());
    }
  }

  public void addComment(Comment comment) {
    boolean hasNullValue = mComments.contains(null);

    removeNullItem();
    List<Comment> oldList = new ArrayList<>();
    oldList.addAll(mComments);
    clearInternal(mComments);
    insertInternal(mComments, comment, 0);
    addComments(oldList);
    if (hasNullValue)
    {
      insertInternal(mComments, null, mComments.size());
    }
    notifyDataSetChanged();
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public int getItemCount() {
    return mComments.size() + 1;
  }

  @Override
  public Enum getEnumFromPosition(int i) {
    if (i == 0)
    {
      return Types.TYPE_POST;
    } else
    {
      return Types.TYPE_COMMENT;
    }
  }

  @Override
  public Enum getEnumFromOrdinal(int i) {
    return Types.values()[i];
  }

  @Override
  public UltimateRecyclerviewViewHolder getViewHolder(View view) {
    return new ViewHolder(view);
  }

  @Override
  public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    View v = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.fragment_tabs, viewGroup, false);
    return new ViewHolder(v);
  }

  @Override
  public int getAdapterItemCount() {
    return mComments.size() + 1;
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  public enum Types {
    TYPE_POST,
    TYPE_COMMENT
  }

  static class ViewHolder extends UltimateRecyclerviewViewHolder {
    public View mView;

    public ViewHolder(View v) {
      super(v);
      mView = v;
    }
  }
}
