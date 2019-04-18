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
import com.collect_up.c_up.model.Shop;
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


public class ShopInfoAdapter extends UltimateViewAdapter<ShopInfoAdapter.ShopInfoViewHolder>
  implements View.OnClickListener, RatingBar.OnRatingBarChangeListener, RatingBar.OnTouchListener {

  private static Context mContext;
  private static Shop mShop;
  private static ItemListener mListener;
  private RatingBar orgRatingBar;

  public ShopInfoAdapter(Context context, Shop shop) {
    mContext = context;
    mShop = shop;
  }

  public void updateDataSet(Shop shop) {
    mShop = shop;
    notifyDataSetChanged();
  }

  @Override
  public ShopInfoAdapter.ShopInfoViewHolder getViewHolder(View view) {
    return new ShopInfoViewHolder(view, false);
  }

  @Override
  public ShopInfoAdapter.ShopInfoViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.fragment_business_tab_info, viewGroup, false);
    ShopInfoViewHolder holder = new ShopInfoViewHolder(view, true);

    holder.complexName.setOnClickListener(this);
    holder.totalFollowers.setOnClickListener(this);
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
  public void onBindViewHolder(final ShopInfoAdapter.ShopInfoViewHolder holder, int position) {
    Hashtable<ShopInfoViewHolder, String> holderAndString = new Hashtable<>(1);
    holderAndString.put(holder, mShop.getId());

    final Hashtable<ShopInfoViewHolder, Shop> holderAndStringRating = new Hashtable<>(1);
    holderAndStringRating.put(holder, mShop);

    DrawableCompat.setTint(holder.ratingBar.getProgressDrawable(), mContext.getResources()
      .getColor(R.color.colorAccent));

    orgRatingBar = holder.ratingBar;
    // Set tags
    holder.totalFollowers.setTag(mShop.getId());
    holderAndString.put(holder, mShop.getFollowers() != null && mShop.getFollowers()
      .contains(Logged.Models.getUserProfile().getId()) ? "true" : "false");
    holder.complexName.setTag(mShop.getComplex());

    if (!Utils.isNullOrEmpty(mShop.getRatesAverage()))
    {
      holder.ratingBar.setRating(Float.valueOf(mShop.getRatesAverage()));
      holder.txtAverageRate.setText(mShop.getRatesAverage().matches("[0-9]+\\.[0-9]*") ? mShop.getRatesAverage() : mShop.getRatesAverage() + ".0");
      holder.totalRates.setText(Utils.getReadableCount(mShop.getRatesCount()));
    }
    setRatingColor(holder.ratingBar, R.color.divider, R.color.rating_color);

    if (mShop.getFollowers() != null)
    {
      holder.totalFollowers.setText(Utils.getReadableCount(mShop.getFollowers().size()));
    } else
    {
      holder.totalFollowers.setText("0");
    }


    if (!Utils.isNullOrEmpty(mShop.getPhoneNumber()))
    {
      holder.phoneNumber.setVisibility(View.VISIBLE);
      holder.phoneNumber.setText(mShop.getPhoneNumber());
    }

    if (!Utils.isNullOrEmpty(mShop.getEmail()))
    {
      holder.emailAddress.setVisibility(View.VISIBLE);
      holder.emailAddress.setText(mShop.getEmail());
    }

    if (!Utils.isNullOrEmpty(mShop.getAddress()))
    {
      holder.address.setVisibility(View.VISIBLE);
      holder.address.setText(mShop.getAddress());
    }

    if (!Utils.isNullOrEmpty(mShop.getDescription()))
    {
      holder.description.setVisibility(View.VISIBLE);
      holder.description.setText(mShop.getDescription());
      holder.description.setTransformationMethod(new LinkTransformationMethod((Activity) mContext));
      holder.description.setMovementMethod(LinkMovementMethod.getInstance());
    }

    if (!Utils.isNullOrEmpty(mShop.getUsername()))
    {
      holder.username.setVisibility(View.VISIBLE);
      holder.username.setText(mShop.getUsername());
    }

    if (mShop.getComplex() != null && !Utils.isNullOrEmpty(mShop.getComplex().getName()))
    {
      holder.complexName.setVisibility(View.VISIBLE);
      holder.complexName.setText(mShop.getComplex().getName());
    }

    holder.follow.setTag(holderAndString);

    if (mShop.getFollowers() != null)
    {
      holder.follow.setText(mShop.getFollowers()
        .contains(Logged.Models.getUserProfile().getId()) ? mContext.getString(R.string.unfollow) : mContext
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
        bar.setOnTouchListener(ShopInfoAdapter.this);
        setRatingColor(bar, R.color.accent_opacity, R.color.colorAccent);

        bar.setOnRatingBarChangeListener(ShopInfoAdapter.this);
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
      case R.id.text_view_complex:
        if (mListener != null)
        {
          Complex complex = (Complex) v.getTag();
          mListener.onComplexClick(complex);
        }
        break;
      case R.id.text_view_total_followers:
        if (mListener != null)
        {
          mListener.onFollowersClick(mShop.getId());
        }
        break;
      case R.id.button_follow:
        Hashtable<ShopInfoViewHolder, String> tags = (Hashtable<ShopInfoViewHolder, String>) v
          .getTag();
        if (mListener != null)
        {
          mListener.onFollowClick((Button) v, tags.keys()
            .nextElement().totalFollowers, mShop);
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
      Hashtable<ShopInfoViewHolder, Shop> tags = (Hashtable<ShopInfoViewHolder, Shop>) ratingBar
        .getTag();
      mListener.onRatingBarChanged(new RatingBar[] {ratingBar, orgRatingBar},
        rating,
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

    void onComplexClick(Complex complex);

    void onFollowersClick(String shopId);

    void onRatingBarChanged(RatingBar[] ratingBar,
                            float rating,
                            boolean fromUser,
                            Shop shop,
                            TextView totalVotesView,
                            TextView averageText);

    void onFollowClick(Button view, TextView totalFollowers, Shop shop);
  }

  public static class ShopInfoViewHolder extends UltimateRecyclerviewViewHolder {

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
    @Bind (R.id.text_view_complex)
    TextView complexName;
    @Bind (R.id.text_view_description)
    TextView description;
    @Bind (R.id.text_view_address)
    TextView address;
    @Bind (R.id.text_view_username)
    TextView username;
    private GoogleMap mGoogleMap;
    @Bind (R.id.layoutRate)
    LinearLayout layoutRate;
    @Bind (R.id.txtAverageRate)
    TextView txtAverageRate;

    public ShopInfoViewHolder(View itemView,
                              boolean isItem) {
      super(itemView);

      if (isItem)
      {
        ButterKnife.bind(this, itemView);

        SupportMapFragment mapFragment = ((SupportMapFragment) ((FragmentActivity) mContext).getSupportFragmentManager()
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

            final LatLng latLng = new LatLng(Double.valueOf(mShop.getLat()), Double.valueOf(mShop.getLong()));
            if (latLng.latitude == 0 && latLng.longitude == 0)
            {
            } else
            {
              mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                .title(mContext.getString(R.string.placed_here)));
              mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mContext
                .getResources()
                .getInteger(R.integer.map_zoom_far)));
            }
            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
              @Override
              public void onMapClick(LatLng mapLatLng) {
                if (mListener != null)
                {
                  mListener.onMapClick(latLng);
                }
              }
            });
          }
        });
      }
    }
  }

}
