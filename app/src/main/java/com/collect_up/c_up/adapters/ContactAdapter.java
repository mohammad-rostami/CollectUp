/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentDisplayFollowersNFollowing;
import com.collect_up.c_up.fragments.FragmentManagers;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;


public class ContactAdapter extends UltimateViewAdapter<ContactAdapter.Holder> implements
  View.OnClickListener {

  private final Fragment mActivity;
  private final List<Profile> mProfiles;
  private final int MODE_FOLLOWING = 0;
  private final int MODE_FOLLOW = 1;
  private final int MODE_REQUESTED = 2;

  public ContactAdapter(Fragment activity, List<Profile> profileList) {
    mActivity = activity;
    mProfiles = profileList;
  }

  /**
   * @param newProfile
   */
  public void update(Profile newProfile, String section) {
    for (Profile profile : mProfiles)
    {
      if (profile.getId().equals(newProfile.getId()))
      {
        int pos = mProfiles.indexOf(profile);
        mProfiles.set(pos, newProfile);
        notifyItemChanged(pos);
        break;
      }
    }

    if (mActivity instanceof FragmentDisplayFollowersNFollowing)
    {
      if (section.equals(mActivity.getString(R.string.following)) && !Logged.Models.getUserProfile().getFollowing().contains(newProfile.getId()))
      {
        for (Profile profile : mProfiles)
        {
          if (profile.getId().equals(newProfile.getId()))
          {
            int pos = mProfiles.indexOf(profile);
            mProfiles.remove(pos);
            notifyItemRemoved(pos);
            break;
          }
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
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.inf_contacts_without_sticky_header, viewGroup, false);

    Holder holder = new Holder(view, true);
    holder.profilePicture.setOnClickListener(this);
    holder.name.setOnClickListener(this);
    holder.follow.setOnClickListener(this);
    holder.username.setOnClickListener(this);

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
  public void onBindViewHolder(Holder holder, int position) {

    if (holder.username == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    Profile item = mProfiles.get(position);
    if (item != null && item.isOfficial())
    {
      holder.imgOfficial.setVisibility(View.VISIBLE);

    } else
    {
      holder.imgOfficial.setVisibility(View.GONE);
    }
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
    if (item.getRequested().contains(Logged.Models.getUserProfile().getId()))
    {
      holder.follow.setVisibility(View.VISIBLE);
      holder.follow.setText(R.string.requested);
      holder.follow.setTag(muteTag);
      changeColorBtnFollowing(holder, MODE_REQUESTED);

    } else if (Logged.Models.getUserProfile().getFollowing().contains(item.getId()))
    {
      holder.follow.setVisibility(View.VISIBLE);
      holder.follow.setText(R.string.following);
      holder.follow.setTag(muteTag);
      changeColorBtnFollowing(holder, MODE_FOLLOWING);
    } else
    {
      holder.follow.setVisibility(View.VISIBLE);
      holder.follow.setText(R.string.follow);
      holder.follow.setTag(muteTag);
      changeColorBtnFollowing(holder, MODE_FOLLOW);
    }

    if (item.getId().equals(Logged.Models.getUserProfile().getId()) || mActivity instanceof FragmentManagers)
    {
      holder.follow.setVisibility(View.INVISIBLE);
    }
  }

  private void changeColorBtnFollowing(Holder holder, int mode) {
    switch (mode)
    {
      case MODE_FOLLOWING:
        holder.follow.setBackgroundColor(ContextCompat.getColor(mActivity.getContext(), android.R.color.white));
        holder.follow.setBackgroundDrawable(ContextCompat.getDrawable(mActivity.getContext(), R.drawable.btn_default_normal_blue_bg));
        holder.follow.setTextColor(ContextCompat.getColor(mActivity.getContext(), android.R.color.white));

        break;
      case MODE_FOLLOW:
        holder.follow.setBackgroundColor(ContextCompat.getColor(mActivity.getContext(), android.R.color.white));
        holder.follow.setBackgroundDrawable(ContextCompat.getDrawable(mActivity.getContext(), R.drawable.btn_default_normal));
        holder.follow.setTextColor(ContextCompat.getColor(mActivity.getContext(), R.color.button_color));


        break;
      case MODE_REQUESTED:
        holder.follow.setBackgroundColor(ContextCompat.getColor(mActivity.getContext(), R.color.divider));

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
        final Profile profile = (Profile) v.getTag();
        if (mActivity instanceof FragmentManagers)
        {

          final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(mActivity.getContext());
          builder.message(R.string.dialog_set_admin)
            .messageTextColor(ContextCompat.getColor(mActivity.getContext(), R.color.primary_text))
            .title(R.string.set_admin)
            .titleColor(ContextCompat.getColor(mActivity.getContext(), R.color.colorAccent))
            .positiveAction(R.string.set)
            .negativeAction(R.string.dismiss)
            .actionTextColor(ContextCompat.getColor(mActivity.getContext(), R.color.colorAccent))
            .setCancelable(true);

          builder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder.dismiss();

            }
          });
          builder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder.dismiss();
              String url;
              if (((FragmentManagers) mActivity).getMode() == FragmentManagers.MODE_SHOP)
              {
                url = Constants.Server.Shop.GET_SET_SHOP_ADMIN;
              } else
              {
                url = Constants.Server.Shop.GET_SET_COMPLEX_ADMIN;

              }

              HttpClient.get(String.format(url, ((FragmentManagers) mActivity).getOwnId(), profile.getId()), new JsonHttpResponser(mActivity.getContext()) {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                  super.onSuccess(statusCode, headers, response);
                  if (response.has("Error"))
                  {
                    String errorString = null;
                    try
                    {
                      errorString = response.getString("Error");
                    } catch (JSONException e)
                    {
                      e.printStackTrace();
                    }
                    final com.rey.material.app.SimpleDialog message = new com.rey.material.app.SimpleDialog(mActivity.getContext());
                    message.message(errorString)
                      .messageTextColor(ContextCompat.getColor(mActivity.getContext(), R.color.primary_text))
                      .title(R.string.set_admin)
                      .titleColor(ContextCompat.getColor(mActivity.getContext(), R.color.colorAccent))
                      .negativeAction(R.string.dismiss)
                      .actionTextColor(ContextCompat.getColor(mActivity.getContext(), R.color.colorAccent))
                      .setCancelable(true);
                    message.show();
                    message.negativeActionClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                        message.dismiss();
                      }
                    });

                  } else
                  {
                    if (((FragmentManagers) mActivity).getMode() == FragmentManagers.MODE_SHOP)

                    {
                      Logged.Models.setUserShop(null);

                    } else
                    {
                      Logged.Models.setUserComplex(null);

                    }
                    Toast.makeText(mActivity.getContext(), R.string.toast_set_admin, Toast.LENGTH_SHORT).show();
                    DrawerHelper.update(DrawerHelper.getDrawer(), mActivity.getActivity());
                    FragmentHandler.onBackPressed(mActivity.getContext());
                  }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                  super.onSuccess(statusCode, headers, response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                  super.onFailure(statusCode, headers, throwable, errorResponse);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                  super.onFailure(statusCode, headers, throwable, errorResponse);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                  super.onFailure(statusCode, headers, responseString, throwable);
                }
              });


            }

          });
          mActivity.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
              builder.show();

            }
          });

        } else

        {
          FragmentHandler.replaceFragment(mActivity.getContext(), fragmentType.PROFILE, profile);
        }
        break;
      case R.id.button_follow:
        final Hashtable<Holder, Profile> tag = (Hashtable<Holder, Profile>) v.getTag();
        final Holder holder = tag.keys().nextElement();
        final Profile profile1 = tag.values().iterator().next();
        if (((android.widget.Button) v).getText() == mActivity.getString(R.string.following))

        {
          holder.follow.setText(R.string.follow);

          HttpClient.get(String.format(Constants.Server.Profile.GET_UNFOLLOW, profile1.getId()), new AsyncHttpResponser(mActivity.getContext()) {
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

              holder.follow.setText(R.string.following);
              changeColorBtnFollowing(holder, MODE_FOLLOWING);
            }
          });
        } else if (((android.widget.Button) v).getText() == mActivity.getString(R.string.follow))

        {

          HttpClient.get(String.format(Constants.Server.Profile.GET_FOLLOW, profile1.getId()), new AsyncHttpResponser(mActivity.getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              if (profile1.getRequested().contains(Logged.Models.getUserProfile().getId()) && profile1.getIsPrivate())
              {
                holder.follow.setText(R.string.requested);
                changeColorBtnFollowing(holder, MODE_REQUESTED);
              } else
              {

                if (profile1.getIsPrivate())
                {
                  if (!profile1.getRequested().contains(Logged.Models.getUserProfile().getId()))
                  {
                    holder.follow.setText(R.string.requested);
                    changeColorBtnFollowing(holder, MODE_REQUESTED);

                  }
                } else
                {
                  holder.follow.setText(R.string.following);
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

              holder.follow.setText(R.string.follow);
              changeColorBtnFollowing(holder, MODE_FOLLOW);
            }
          });
        } else

        {
          holder.follow.setText(R.string.requested);

          HttpClient.get(String.format(Constants.Server.Request.CANCEL_REQUEST, profile1.getId()), new AsyncHttpResponser(mActivity.getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              holder.follow.setText(R.string.follow);
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

              holder.follow.setText(R.string.requested);
              changeColorBtnFollowing(holder, MODE_FOLLOW);
            }
          });
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
    @Bind (R.id.button_follow)
    Button follow;
    @Bind (R.id.imgOfficial)
    ImageView imgOfficial;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
