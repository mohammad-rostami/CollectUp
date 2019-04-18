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
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.google.android.gms.maps.model.LatLng;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PersonInfoAdapter extends UltimateViewAdapter<PersonInfoAdapter.ViewHolder>
  implements View.OnClickListener {

  private final Context mContext;
  private Profile mProfile;
  private OnItemClick mListener;

  public PersonInfoAdapter(Context context, Profile profile) {
    mContext = context;
    mProfile = profile;
  }

  public void updateDataSet(Profile profile) {
    mProfile = profile;
    notifyDataSetChanged();
  }

  @Override
  public ViewHolder getViewHolder(View view) {
    return new ViewHolder(view, false);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    View v = LayoutInflater.from(mContext)
      .inflate(R.layout.fragment_person_tab_info, viewGroup, false);

    ViewHolder holder = new ViewHolder(v, true);
    holder.followers.setOnClickListener(this);
    holder.following.setOnClickListener(this);
    holder.follow.setOnClickListener(this);

    return holder;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, int position) {
    if (mProfile.getFollowers() != null)
    {
      viewHolder.followers.setText(Utils.getReadableCount(mProfile.getFollowers().size()));
    } else
    {
      viewHolder.followers.setText("-");
    }
    if (mProfile.getFollowing() != null)
    {
      viewHolder.following.setText(Utils.getReadableCount(mProfile.getFollowing().size()));
    } else
    {
      viewHolder.following.setText("-");
    }

    if (Logged.Models.getUserProfile().getId().equals(mProfile.getId()))
    {
      viewHolder.follow.setBackgroundColor(mContext.getResources().getColor(R.color.divider));
      viewHolder.follow.setText(R.string.action_update_info);
      viewHolder.follow.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentHandler.replaceFragment(mContext, fragmentType.EDITPERSON, null);
        }
      });
    }

    Hashtable<ViewHolder, String> followTags = new Hashtable<>(1);
    if (mProfile.getFollowers() != null)
    {
      if (mProfile.getFollowers().contains(Logged.Models.getUserProfile().getId()))
      {
        viewHolder.follow.setText(mContext.getString(R.string.unfollow));
        followTags.put(viewHolder, "true");
        viewHolder.follow.setTag(followTags);
      } else if (mProfile.getRequested().contains(Logged.Models.getUserProfile().getId()))
      {
        viewHolder.follow.setText(R.string.requested);
        viewHolder.follow.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
        followTags.put(viewHolder, "requested");
        viewHolder.follow.setTag(followTags);

      } else
      {
        followTags.put(viewHolder, "false");
        viewHolder.follow.setTag(followTags);
      }
    } else
    {
      followTags.put(viewHolder, "false");
      viewHolder.follow.setTag(followTags);
    }


    viewHolder.followers.setTag(mProfile.getId());
    viewHolder.following.setTag(mProfile.getId());

    if (!isAccountPrivate() && !Utils.isNullOrEmpty(mProfile.getJob()) && mProfile.getSettingsJob())
    {
      viewHolder.job.setText(mProfile.getJob());
      viewHolder.job.setVisibility(View.VISIBLE);
    }

    if (!isAccountPrivate() && !Utils.isNullOrEmpty(mProfile.getUsername()) && mProfile.getSettingsUsername())
    {
      viewHolder.username.setVisibility(View.VISIBLE);
      viewHolder.username.setText(mProfile.getUsername());
    }

    if (!isAccountPrivate() && !Utils.isNullOrEmpty(mProfile.getPhoneNumber()) && (SepehrUtil.contactExists(this.mContext, mProfile.getPhoneNumber()) || mProfile.getSettingsPhoneNumber()))
    {
      viewHolder.phoneNumber.setVisibility(View.VISIBLE);
      viewHolder.phoneNumber.setText(mProfile.getPhoneNumber());
    }

    if (!isAccountPrivate() && mProfile.getSettingsGender())
    {
      viewHolder.gender.setVisibility(View.VISIBLE);
      // Attention: These numbers are based-on the gender array string
      viewHolder.gender.setText(mProfile.getIsMan() ? R.string.male : R.string.female);
    }

    if (!isAccountPrivate() && !Utils.isNullOrEmpty(mProfile.getLat()) && !mProfile.getLat().equals("0") && !Utils.isNullOrEmpty(mProfile.getLong()) && !mProfile.getLong().equals("0") && mProfile.getSettingsLocation())
    {
      viewHolder.location.setVisibility(View.VISIBLE);

      AsyncTask<Void, Void, String> getLocationNameAsync = new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
          return Location.getCountryAndCity(mContext, new LatLng(Double.valueOf(mProfile.getLat()), Double.valueOf(mProfile.getLong())));
        }

        @Override
        protected void onPostExecute(String s) {
          super.onPostExecute(s);
          if (!Utils.isNullOrEmpty(s))
          {
            viewHolder.location.setText(s);
          } else
          {
            viewHolder.location.setVisibility(View.GONE);
          }
        }
      };
      getLocationNameAsync.execute();
    }

    if (!isAccountPrivate() && mProfile.getLanguages() != null && mProfile.getLanguages().size() > 0 && mProfile.getSettingsLanguages())
    {
      viewHolder.languages.setVisibility(View.VISIBLE);
      viewHolder.languages.setText(TextUtils.join(", ", mProfile.getLanguages()));
    }

    if (!isAccountPrivate() && !Utils.isNullOrEmpty(mProfile.getBirthday()) && mProfile.getSettingsBirthday())
    {
      viewHolder.birthday.setVisibility(View.VISIBLE);
      if (mProfile.getBirthday().contains("T"))
      {
        String serverDate = TimeHelper.getServerStringDate(TimeHelper.getDateFromServerDatePattern(mContext, mProfile.getBirthday()));
        Date date = TimeHelper.getDateFromServerDatePattern(mContext, serverDate);
        viewHolder.birthday.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format
          (date));
      } else
      {
        try
        {
          Date date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(mProfile.getBirthday());
          viewHolder.birthday.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format
            (date));
        } catch (ParseException e)
        {
        }

      }
    }


    if (!Utils.isNullOrEmpty(mProfile.getBiography()) && mProfile.getSettingsBiography())
    {
      viewHolder.biography.setText(mProfile.getBiography());
      viewHolder.biography.setTransformationMethod(new LinkTransformationMethod((Activity) mContext));
      viewHolder.biography.setMovementMethod(LinkMovementMethod.getInstance());
      viewHolder.biography.setVisibility(View.VISIBLE);
    }
  }

  public void setListener(OnItemClick listener) {
    mListener = listener;
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
    return 1;
  }

  @Override
  public int getAdapterItemCount() {
    return 1;
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  private boolean isAccountPrivate() {
    return mProfile.getIsPrivate() && !mProfile.getId().equals(Logged.Models.getUserProfile().getId()) && !Logged.Models.getUserProfile().getFollowing().contains(mProfile.getId());
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.text_view_followers:
        if (!isAccountPrivate())
        {
          if (mListener != null)
          {
            String profileId = (String) v.getTag();
            mListener.OnFollowersClick(profileId);
          }
        }
        break;
      case R.id.text_view_following:
        if (!isAccountPrivate())
        {
          if (mListener != null)
          {
            String profileId = (String) v.getTag();
            mListener.OnFollowingClick(profileId);
          }
        }
        break;
      case R.id.button_follow:
        Hashtable<ViewHolder, String> tag = (Hashtable<ViewHolder, String>) v.getTag();
        if (mListener != null)
        {
          mListener.onFollowClick((Button) v, tag.keySet()
            .iterator()
            .next().followers, mProfile.getId());
        }
        break;
    }
  }

  public interface OnItemClick {
    void OnFollowersClick(String profileId);

    void OnFollowingClick(String profileId);

    void onFollowClick(Button view, TextView totalFollowersView, String profileId);
  }

  public static class ViewHolder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_followers)
    TextView followers;
    @Bind (R.id.text_view_following)
    TextView following;
    @Bind (R.id.button_follow)
    Button follow;
    @Bind (R.id.text_view_job)
    TextView job;
    @Bind (R.id.text_view_birthday)
    TextView birthday;
    @Bind (R.id.text_view_biography)
    TextView biography;
    @Bind (R.id.text_view_username)
    TextView username;
    @Bind (R.id.text_view_gender)
    TextView gender;
    @Bind (R.id.text_view_phone_number)
    TextView phoneNumber;
    @Bind (R.id.text_view_location)
    TextView location;
    @Bind (R.id.text_view_languages)
    TextView languages;

    public ViewHolder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
