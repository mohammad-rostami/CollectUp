/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.Button;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BlockedListAdapter extends UltimateViewAdapter<BlockedListAdapter.Holder>
  implements View.OnClickListener {

  private final Context mContext;
  public List<Profile> mProfiles;
  private OnViewClickListener mOnViewClickListener;
  private int mMode;
  public static final int MODE_UNBLOCK = 0;
  public static final int MODE_ALLOW = 1;

  public BlockedListAdapter(Context context, List<Profile> profiles, int mode) {
    mContext = context;
    mProfiles = profiles;
    mMode = mode;
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  public void setOnViewClickListener(OnViewClickListener listener) {
    if (listener != null)
    {
      mOnViewClickListener = listener;
    }
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View v = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.inf_follow_and_deny, viewGroup, false);

    Holder holder = new Holder(v, true);

    holder.name.setOnClickListener(this);
    holder.picture.setOnClickListener(this);
    holder.btnBlock.setOnClickListener(this);
    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mProfiles.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(final Holder holder, int position) {
    final Profile item = mProfiles.get(position);

    holder.name.setText(item.getName());
    holder.txtUser.setText(item.getUsername());
    if (mMode == MODE_UNBLOCK)
    {
      holder.btnBlock.setText(mContext.getResources().getString(R.string.unblock));
    } else
    {
      holder.btnBlock.setText(mContext.getResources().getString(R.string.allow));
    }

    holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false)
      .setText(item.getName())
      .setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb());
    holder.btnBlock.setTag(item);
    holder.picture.setTag(item);
    holder.name.setTag(item);
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
  }


  @Override
  public void onClick(View v) {
    if (mOnViewClickListener != null)
    {
      Profile profile = (Profile) v.getTag();
      mOnViewClickListener.onClick(profile, v.getId(), mProfiles.indexOf(profile));
    }
  }


  public interface OnViewClickListener {
    void onClick(Profile profile, int viewId, int post);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {

    @Bind (R.id.image_view_picture)
    ComplexAvatarView picture;
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.text_view_username)
    TextView txtUser;
    @Bind (R.id.button_following)
    Button btnBlock;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
