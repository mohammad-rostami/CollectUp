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
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.app.BottomSheetDialog;

import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ComplexInfoAdapter
  extends UltimateViewAdapter<ComplexInfoAdapter.ComplexInfoViewHolder>
  implements View.OnClickListener, RatingBar.OnRatingBarChangeListener, RatingBar.OnTouchListener {

  private static Complex mComplex;
  private final Context mContext;
  private final Profile mCurrentUser = Logged.Models.getUserProfile();
  private ItemListener mListener;
  private RatingBar orgRatingBar;

  public ComplexInfoAdapter(Context context, Complex complex) {
    mContext = context;
    mComplex = complex;
  }

  public void updateDataSet(Complex complex) {
    mComplex = complex;
    notifyDataSetChanged();
  }

  @Override
  public ComplexInfoViewHolder getViewHolder(View view) {
    return new ComplexInfoViewHolder(view, false, mContext, mListener);
  }

  @Override
  public ComplexInfoViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(mContext)
      .inflate(R.layout.fragment_complex_tab_info, viewGroup, false);
    ComplexInfoViewHolder holder = new ComplexInfoViewHolder(view, true, mContext, mListener);

    holder.totalFollowers.setOnClickListener(this);
    holder.ratingBar.setOnRatingBarChangeListener(this);
    holder.follow.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return 1;
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(ComplexInfoViewHolder holder, int position) {
    Hashtable<ComplexInfoViewHolder, String> holderAndString = new Hashtable<>(1);
    holderAndString.put(holder, mComplex.getId());

    final Hashtable<ComplexInfoViewHolder, Complex> holderAndStringRating = new Hashtable<>(1);
    holderAndStringRating.put(holder, mComplex);

    DrawableCompat.setTint(holder.ratingBar.getProgressDrawable(), mContext.getResources()
      .getColor(R.color.colorAccent));
    orgRatingBar = holder.ratingBar;

    // Set tags
    holder.totalFollowers.setTag(mComplex.getId());
    holderAndString.put(holder, mComplex.getFollowers() != null && mComplex.getFollowers()
      .contains(mCurrentUser
        .getId()) ? "true" : "false");
    holder.follow.setTag(holderAndString);

    if (!Utils.isNullOrEmpty(mComplex.getRatesAverage()))
    {
      holder.txtAverageRate.setText(mComplex.getRatesAverage().matches("[0-9]+\\.[0-9]*") ? mComplex.getRatesAverage() : mComplex.getRatesAverage() + ".0");
      holder.totalRates.setText(Utils.getReadableCount(mComplex.getRatesCount()));
      holder.ratingBar.setRating(Float.valueOf(mComplex.getRatesAverage()));
    } else
    {
      holder.ratingBar.setRating(0F);
    }
    setRatingColor(holder.ratingBar, R.color.divider, R.color.rating_color);

    if (mComplex.getFollowers() != null)
    {
      holder.totalFollowers.setText(Utils.getReadableCount(mComplex.getFollowers().size()));
    } else
    {
      holder.totalFollowers.setText("0");
    }


    if (!Utils.isNullOrEmpty(mComplex.getPhoneNumber()))
    {
      holder.phoneNumber.setVisibility(View.VISIBLE);
      holder.phoneNumber.setText(mComplex.getPhoneNumber());
    }

    if (!Utils.isNullOrEmpty(mComplex.getUsername()))
    {
      holder.username.setVisibility(View.VISIBLE);
      holder.username.setText(mComplex.getUsername());
    }

    if (!Utils.isNullOrEmpty(mComplex.getEmail()))
    {
      holder.emailAddress.setVisibility(View.VISIBLE);
      holder.emailAddress.setText(mComplex.getEmail());
    }

    if (!Utils.isNullOrEmpty(mComplex.getAddress()))
    {
      holder.address.setVisibility(View.VISIBLE);
      holder.address.setText(mComplex.getAddress());
    }

    if (!Utils.isNullOrEmpty(mComplex.getDescription()))
    {
      holder.description.setVisibility(View.VISIBLE);
      holder.description.setText(mComplex.getDescription());
      holder.description.setTransformationMethod(new LinkTransformationMethod((Activity) mContext));
      holder.description.setMovementMethod(LinkMovementMethod.getInstance());
    }

    if (mComplex.getFollowers() != null)
    {
      holder.follow.setText(mComplex.getFollowers()
        .contains(mCurrentUser.getId()) ? mContext.getString(R.string.unfollow) : mContext
        .getString(R.string.follow));
    } else
    {
      holder.follow.setText(mContext.getString(R.string.follow));
    }
    holder.layoutRate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(mContext);

        mSheetDialog.contentView(R.layout.bottom_sheet_rate)
          .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
          .inDuration(300)
          .cancelable(true)
          .show();
        RatingBar bar = (RatingBar) mSheetDialog.findViewById(R.id.rating_bar);
        bar.setOnTouchListener(ComplexInfoAdapter.this);
        setRatingColor(bar, R.color.accent_opacity, R.color.colorAccent);

        bar.setOnRatingBarChangeListener(ComplexInfoAdapter.this);
        bar.setTag(holderAndStringRating);

      }
    });
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  public void setClickListener(ItemListener listener) {
    mListener = listener;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.text_view_total_followers:
        if (mListener != null)
        {
          mListener.onFollowersClick(mComplex.getId());
        }
        break;
      case R.id.button_follow:
        Hashtable<ComplexInfoViewHolder, String> tags = (Hashtable<ComplexInfoViewHolder, String>) v
          .getTag();
        if (mListener != null)
        {
          mListener.onFollowClick((Button) v, tags.keys()
            .nextElement().totalFollowers, mComplex);
        }
        break;
    }
  }

  public void setRatingColor(RatingBar bar, int colorNormal, int colorFill) {
    LayerDrawable stars = (LayerDrawable) bar.getProgressDrawable();
    stars.getDrawable(0).setColorFilter(ContextCompat.getColor(mContext, colorNormal), PorterDuff.Mode.SRC_ATOP);
    stars.getDrawable(2).setColorFilter(ContextCompat.getColor(mContext, colorFill), PorterDuff.Mode.SRC_ATOP);

  }

  @SuppressWarnings ("unchecked")
  @Override
  public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
    if (mListener != null && fromUser)
    {
      Hashtable<ComplexInfoViewHolder, Complex> tags = (Hashtable<ComplexInfoViewHolder, Complex>) ratingBar
        .getTag();
      mListener.onRatingBarChanged(new RatingBar[] {ratingBar, orgRatingBar}, rating,
        true,
        tags.values().iterator().next(),
        tags.keySet().iterator().next().totalRates,
        tags.keySet().iterator().next().txtAverageRate);
    }
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    setRatingColor((RatingBar) v, R.color.accent_opacity, R.color.colorAccent);

    return false;
  }

  public interface ItemListener {
    void onMapClick(LatLng latLng);

    void onFollowersClick(String complexId);

    void onRatingBarChanged(RatingBar[] ratingBar,
                            float rating,
                            boolean fromUser,
                            Complex complex,
                            TextView totaFlVotesView,
                            TextView txtAverage);

    void onFollowClick(Button view, TextView totalFollowers, Complex complex);
  }

  public static class ComplexInfoViewHolder extends UltimateRecyclerviewViewHolder {

    @Bind (R.id.text_view_votes)
    TextView totalRates;
    @Bind (R.id.text_view_total_followers)
    TextView totalFollowers;
    @Bind (R.id.rating_bar)
    RatingBar ratingBar;
    @Bind (R.id.button_follow)
    Button follow;
    @Bind (R.id.text_view_email)
    TextView emailAddress;
    @Bind (R.id.text_view_phone_number)
    TextView phoneNumber;
    @Bind (R.id.text_view_description)
    TextView description;
    @Bind (R.id.text_view_username)
    TextView username;
    @Bind (R.id.text_view_address)
    TextView address;
    private GoogleMap mGoogleMap;
    @Bind (R.id.layoutRate)
    LinearLayout layoutRate;
    @Bind (R.id.txtAverageRate)
    TextView txtAverageRate;

    public ComplexInfoViewHolder(View itemView,
                                 boolean isItem,
                                 final Context context,
                                 final ItemListener itemListener) {
      super(itemView);

      if (isItem)
      {
        ButterKnife.bind(this, itemView);

        SupportMapFragment mapFragment = ((SupportMapFragment) ((FragmentActivity) context).getSupportFragmentManager()
          .findFragmentById(R.id.map_view));

        mapFragment.getMapAsync(new OnMapReadyCallback() {
          @Override
          public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

            final LatLng latLng = new LatLng(Double.valueOf(mComplex.getLat()), Double.valueOf(mComplex.getLong()));
            if (latLng.latitude == 0 && latLng.longitude == 0)
            {
            } else
            {
              mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                .title(context.getString(R.string.placed_here)));
              mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, context
                .getResources()
                .getInteger(R.integer.map_zoom_far)));
            }
            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
              @Override
              public void onMapClick(LatLng mapLatLng) {
                if (itemListener != null)
                {
                  itemListener.onMapClick(latLng);
                }
              }
            });
          }
        });
      }
    }
  }

}
