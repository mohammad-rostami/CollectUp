/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentDisplayFollowersNFollowing;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.OnItemClickListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.Hashtable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MentionAdapter extends UltimateViewAdapter<MentionAdapter.Holder> implements
  View.OnClickListener {

  private final Fragment mActivity;
  private final OnItemClickListener mListener;
  private final Pagination.MentionMode mMentionMode;
  List<Profile> mProfileList;
  List<Shop> mShopList;
  List<CheckOut> mHashtagList;


  public MentionAdapter(Fragment activity, List<Profile> profileList, Pagination.MentionMode mode, OnItemClickListener listenr) {
    mActivity = activity;
    mListener = listenr;
    mMentionMode = mode;
    mProfileList = profileList;
  }

  public MentionAdapter(Fragment activity, Pagination.MentionMode mode, List<Shop> shopList, OnItemClickListener listenr) {
    mActivity = activity;
    mListener = listenr;
    mMentionMode = mode;
    mShopList = shopList;
  }

  public MentionAdapter(Fragment activity, List<CheckOut> hashtagList, OnItemClickListener listenr) {
    mActivity = activity;
    mListener = listenr;
    mHashtagList = hashtagList;
    mMentionMode = Pagination.MentionMode.HASHTAG;
  }

  /**
   * @param newProfile
   */
  public void update(Profile newProfile, String section) {
    for (Profile profile : mProfileList)
    {
      if (profile.getId().equals(newProfile.getId()))
      {
        int pos = mProfileList.indexOf(profile);
        mProfileList.set(pos, newProfile);
        notifyItemChanged(pos);
        break;
      }
    }

    if (mActivity instanceof FragmentDisplayFollowersNFollowing)
    {
      if (section.equals(mActivity.getString(R.string.following)) && !Logged.Models.getUserProfile().getFollowing().contains(newProfile.getId()))
      {
        for (Profile profile : mProfileList)
        {
          if (profile.getId().equals(newProfile.getId()))
          {
            int pos = mProfileList.indexOf(profile);
            mProfileList.remove(pos);
            notifyItemRemoved(pos);
            break;
          }
        }
      }
    }
  }

  /**
   * @param newShop
   */
  public void update(Shop newShop, String section) {
    for (Shop shop : mShopList)
    {
      if (shop.getId().equals(newShop.getId()))
      {
        int pos = mShopList.indexOf(shop);
        mShopList.set(pos, newShop);
        notifyItemChanged(pos);
        break;
      }
    }

    if (mActivity instanceof FragmentDisplayFollowersNFollowing)
    {
      for (Shop shop : mShopList)
      {
        if (shop.getId().equals(newShop.getId()))
        {
          int pos = mShopList.indexOf(shop);
          mShopList.remove(pos);
          notifyItemRemoved(pos);
          break;
        }
      }

    }
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mention_list, viewGroup, false);
    Holder holder = new Holder(view, true);
    holder.profilePicture.setOnClickListener(this);
    holder.name.setOnClickListener(this);
    holder.username.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    switch (mMentionMode)
    {
      case BUSINESS:
        return mShopList.size();

      case USER:
        return mProfileList.size();

      case HASHTAG:
        return mHashtagList.size();

    }
    return 0;
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.username == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }
    switch (mMentionMode)
    {
      case USER:
        final Profile item = mProfileList.get(position);

        if (!Utils.isNullOrEmpty(item.getImageAddress()))
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(item.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb());
        } else
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setText(item.getName());
        }

        holder.profilePicture.setTag(item);
        holder.name.setTag(item);
        holder.username.setTag(item);

        holder.name.setText(item.getName());
        holder.username.setText(item.getUsername());

        Hashtable<Holder, Profile> muteTag = new Hashtable<>();
        muteTag.put(holder, item);
        break;
      case BUSINESS:
        final Shop shopItem = mShopList.get(position);

        if (!Utils.isNullOrEmpty(shopItem.getImageAddress()))
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(shopItem.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + shopItem.getThumb());
        } else
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setText(shopItem.getName());
        }

        holder.profilePicture.setTag(shopItem);
        holder.name.setTag(shopItem);
        holder.username.setTag(shopItem);

        holder.name.setText(shopItem.getName());
        holder.username.setText(shopItem.getUsername());

        Hashtable<Holder, Shop> muteTagShop = new Hashtable<>();
        muteTagShop.put(holder, shopItem);
        break;

      case HASHTAG:
        final CheckOut hashtagItem = mHashtagList.get(position);
        holder.profilePicture.setTag(hashtagItem);
        holder.name.setTag(hashtagItem);
        holder.username.setTag(hashtagItem);

        holder.name.setText("#" + hashtagItem.getValue());
        holder.username.setText(hashtagItem.getId() + "");
        holder.profilePicture.setText("#");
        Hashtable<Holder, String> muteTagHashtag = new Hashtable<>();
        muteTagHashtag.put(holder, hashtagItem.getValue());
        break;
    }


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
    switch (v.getId())
    {
      case R.id.image_view_picture:
      case R.id.text_view_name:
      case R.id.text_view_username:
        switch (mMentionMode)
        {
          case USER:
            mListener.onClick((Profile) v.getTag());

            break;
          case BUSINESS:
            mListener.onClick((Shop) v.getTag());

            break;
          case HASHTAG:
            mListener.onClick(((CheckOut) v.getTag()).getValue());

            break;
        }
        break;
    }
  }

  static class Holder extends UltimateRecyclerviewViewHolder {

    @Bind (R.id.image_view_picture)
    ComplexAvatarView profilePicture;
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.text_view_username)
    TextView username;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
