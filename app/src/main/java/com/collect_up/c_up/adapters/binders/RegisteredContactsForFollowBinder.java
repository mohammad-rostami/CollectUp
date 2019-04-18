/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.binders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;
import com.rey.material.widget.Button;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class RegisteredContactsForFollowBinder extends DataBinder<RegisteredContactsForFollowBinder.Holder>
  implements View.OnClickListener {
  private final Context mContext;
  private List<Profile> dataSet;
  private List<String> followingProfileIds = new ArrayList<>();
  private boolean isSearch;
  private final int MODE_FOLLOWING = 0;
  private final int MODE_FOLLOW = 1;
  private final int MODE_REQUESTED = 2;

  public RegisteredContactsForFollowBinder(UltimateDifferentViewTypeAdapter dataBindAdapter,
                                           List<Profile> dataSet,
                                           Context context) {
    super(dataBindAdapter);
    this.dataSet = dataSet;
    this.mContext = context;

    followingProfileIds.addAll(Logged.Models.getUserProfile().getFollowing());
  }

  public void setDataSet(List<Profile> profiles) {
    this.dataSet = profiles;
  }

  @Override
  public Holder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.inf_follow_and_deny, parent, false);

    Holder holder = new Holder(view);

    holder.mTextViewName.setOnClickListener(this);
    holder.mTextViewUsername.setOnClickListener(this);
    holder.mImageViewPicture.setOnClickListener(this);
    holder.mButtonFollowing.setOnClickListener(this);

    return new Holder(view);
  }

  @Override
  public void bindViewHolder(Holder holder, int position) {
    try
    {

      final Profile item = dataSet.get(position);
      Hashtable<Holder, Profile> muteTag = new Hashtable<>();
      muteTag.put(holder, item);

      holder.mTextViewName.setText(item.getName());
      holder.mTextViewUsername.setText(item.getUsername());

      holder.mTextViewName.setTag(item);
      holder.mTextViewUsername.setTag(item);
      holder.mImageViewPicture.setTag(item);
      if (item != null && item.isOfficial())
      {
        holder.imgOfficial.setVisibility(View.VISIBLE);

      } else
      {
        holder.imgOfficial.setVisibility(View.GONE);
      }
      setProfilePicture(item, holder);
      if (item.getRequested().contains(Logged.Models.getUserProfile().getId()))
      {
        holder.mButtonFollowing.setVisibility(View.VISIBLE);
        holder.mButtonFollowing.setText(R.string.requested);
        holder.mButtonFollowing.setTag(muteTag);
        changeColorBtnFollowing(holder, MODE_REQUESTED);

      } else if (followingProfileIds.contains(item.getId()))
      {
        holder.mButtonFollowing.setText(R.string.following);
        holder.mButtonFollowing.setTag(muteTag);
        holder.mButtonFollowing.setAllCaps(false);
        changeColorBtnFollowing(holder, MODE_FOLLOWING);
      } else
      {
        holder.mButtonFollowing.setText(R.string.follow);
        holder.mButtonFollowing.setTag(muteTag);
        changeColorBtnFollowing(holder, MODE_FOLLOW);
      }

    } catch (Exception ex)
    {
      return;
    }
  }

  private void setProfilePicture(Profile profile, Holder holder) {
    if (!Utils.isNullOrEmpty(profile.getImageAddress()))
    {
      holder.mImageViewPicture.makeAllDefaults();
      holder.mImageViewPicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(profile.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + profile.getThumb());
    } else
    {
      holder.mImageViewPicture.makeAllDefaults();
      holder.mImageViewPicture.setText(profile.getName());
    }
  }

  private void changeColorBtnFollowing(Holder holder, int mode) {
    switch (mode)
    {
      case MODE_FOLLOWING:
        holder.mButtonFollowing.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        holder.mButtonFollowing.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_default_normal_blue_bg));
        holder.mButtonFollowing.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));

        break;
      case MODE_FOLLOW:
        holder.mButtonFollowing.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        holder.mButtonFollowing.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_default_normal));
        holder.mButtonFollowing.setTextColor(ContextCompat.getColor(mContext, R.color.button_color));


        break;
      case MODE_REQUESTED:
        holder.mButtonFollowing.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));

        break;
    }
  }

  @Override
  public int getItemCount() {
    if (isSearch)
    {
      return dataSet.size();
    } else
    {
      return 0;

    }
  }

  public void isSearch(boolean searched) {
    isSearch = searched;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.text_view_name:
      case R.id.text_view_username:
      case R.id.image_view_picture:
        Profile profile = (Profile) v.getTag();
        FragmentHandler.replaceFragment(mContext, fragmentType.PROFILE, profile);

        break;
      case R.id.button_following:
        final Hashtable<Holder, Profile> tag = (Hashtable<Holder, Profile>) v.getTag();
        final Holder holder = tag.keys().nextElement();


        final Profile profile1 = tag.values().iterator().next();
        if (((android.widget.Button) v).getText() == mContext.getString(R.string.following))
        {
          holder.mButtonFollowing.setText(R.string.follow);

          HttpClient.get(String.format(Constants.Server.Profile.GET_UNFOLLOW, profile1.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              Profile modifiedProfile = Logged.Models.getUserProfile();
              modifiedProfile.getFollowing().remove(profile1.getId());
              Logged.Models.setUserProfile(modifiedProfile);
              profile1.getFollowers().remove(Logged.Models.getUserProfile().getId());
              changeColorBtnFollowing(holder, MODE_FOLLOW);

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

              holder.mButtonFollowing.setText(R.string.following);
              changeColorBtnFollowing(holder, MODE_FOLLOWING);
            }
          });
        } else if (((android.widget.Button) v).getText() == mContext.getString(R.string.follow))
        {

          HttpClient.get(String.format(Constants.Server.Profile.GET_FOLLOW, profile1.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              if (profile1.getRequested().contains(Logged.Models.getUserProfile().getId()) && profile1.getIsPrivate())
              {
                holder.mButtonFollowing.setText(R.string.requested);
                changeColorBtnFollowing(holder, MODE_REQUESTED);
              } else
              {

                if (profile1.getIsPrivate())
                {
                  if (!profile1.getRequested().contains(Logged.Models.getUserProfile().getId()))
                  {
                    holder.mButtonFollowing.setText(R.string.requested);
                    changeColorBtnFollowing(holder, MODE_REQUESTED);

                  }
                } else
                {
                  holder.mButtonFollowing.setText(R.string.following);
                  Profile modifiedProfile = Logged.Models.getUserProfile();
                  modifiedProfile.getFollowing().add(profile1.getId());
                  Logged.Models.setUserProfile(modifiedProfile);
                  profile1.getFollowers().add(Logged.Models.getUserProfile().getId());
                  changeColorBtnFollowing(holder, MODE_FOLLOWING);
                }
              }

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

              holder.mButtonFollowing.setText(R.string.follow);
              changeColorBtnFollowing(holder, MODE_FOLLOW);
            }
          });
        } else
        {
          holder.mButtonFollowing.setText(R.string.requested);

          HttpClient.get(String.format(Constants.Server.Request.CANCEL_REQUEST, Logged.Models.getUserProfile().getId(), profile1.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              holder.mButtonFollowing.setText(R.string.follow);
              Profile myProfile = Logged.Models.getUserProfile();
              myProfile.getRequested().remove(Logged.Models.getUserProfile().getId());
              changeColorBtnFollowing(holder, MODE_FOLLOW);

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

              holder.mButtonFollowing.setText(R.string.requested);
              changeColorBtnFollowing(holder, MODE_FOLLOW);
            }
          });
        }
        break;
    }
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    ComplexAvatarView mImageViewPicture;
    TextView mTextViewName;
    Button mButtonFollowing;
    TextView mTextViewUsername;
    ImageView imgOfficial;

    public Holder(View view) {
      super(view);

      mImageViewPicture = (ComplexAvatarView) view.findViewById(R.id.image_view_picture);
      mTextViewName = (TextView) view.findViewById(R.id.text_view_name);
      mTextViewUsername = (TextView) view.findViewById(R.id.text_view_username);
      mButtonFollowing = (Button) view.findViewById(R.id.button_following);
      imgOfficial = (ImageView) view.findViewById(R.id.imgOfficial);
    }
  }
}
