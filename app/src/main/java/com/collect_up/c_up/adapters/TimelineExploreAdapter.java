/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.pagers.TimelinePagerFragment;
import com.collect_up.c_up.helpers.Constants.General;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Post;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineExploreAdapter extends UltimateViewAdapter<TimelineExploreAdapter.Holder>
  implements View.OnClickListener {

  final private List<Post> mPosts;
  private final int mImageWidth;
  private final Context _Context;
  private ItemListener mListener;

  public TimelineExploreAdapter(List<Post> posts, Context context) {
    this.mPosts = posts;
    _Context = context;
    mImageWidth = MyApplication.getInstance().getApplicationContext().getResources().getDisplayMetrics().widthPixels / 2;
  }

  public void updatePost(Post newPost, boolean deleted) {
    for (Post post : mPosts)
    {
      if (post.getId().equals(newPost.getId()))
      {
        int pos = mPosts.indexOf(post);
        if (deleted)
        {
          mPosts.remove(pos);
          notifyItemRemoved(pos);
        } else
        {
          mPosts.set(pos, newPost);
          notifyItemChanged(pos);
        }
        break;
      }
    }
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_grid_timeline_tab_world, parent, false);
    Holder holder = new Holder(view, true);
    holder.picture.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mPosts.size();
  }

  @Override
  public long generateHeaderId(int position) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.picture == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    Post item = mPosts.get(position);
    int itemSize = SepehrUtil.getScreenWith((Activity) _Context) / TimelinePagerFragment.mColumns;
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(itemSize, itemSize);

    holder.picture.setTag(item);

    setPicture(holder, item);

    if (Utils.isNullOrEmpty(item.getVideoAddress()))
    {
      holder.videoIcon.setVisibility(View.GONE);
    } else
    {
      holder.videoIcon.setVisibility(View.VISIBLE);
    }

    holder.picture.setLayoutParams(params);
  }

  private void setPicture(final Holder holder, Post post) {
    String imagePath = General.BLOB_PROTOCOL + post.getThumb();

    holder.picture.setImageResource(R.drawable.placeholder);
    MyApplication.getInstance().getImageLoader().displayImage(imagePath, holder.picture, MyApplication.imageOptionsWithAnimation);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
        Post product = (Post) v.getTag();
        if (mListener != null)
        {
          int position = 0;
          for (int i = 0; i < mPosts.size(); i++)
          {
            if (mPosts.get(i) == product)
            {
              position = i;
              break;
            }
          }
          mListener.onPostImageClick(product, position);
        }
        break;
    }
  }

  @Override
  public UltimateRecyclerviewViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

  }

  public void setListener(ItemListener listener) {
    mListener = listener;
  }

  public interface ItemListener {
    void onPostImageClick(Post post, int position);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.image_view_picture)
    ImageView picture;
    @Bind (R.id.image_view_video_icon)
    ImageView videoIcon;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}