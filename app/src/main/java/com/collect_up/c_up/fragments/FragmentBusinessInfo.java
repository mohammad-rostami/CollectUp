package com.collect_up.c_up.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rey.material.app.BottomSheetDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 8/14/2016.
 */
public class FragmentBusinessInfo extends Fragment implements RatingBar.OnRatingBarChangeListener, RatingBar.OnTouchListener {
    public Shop mShop;
    @Bind(R.id.text_view_votes)
    TextView totalRates;
    @Bind(R.id.text_view_total_followers)
    TextView totalFollowers;
    @Bind(R.id.rating_bar)
    RatingBar ratingBarShop;
    @Bind(R.id.button_follow)
    Button follow;
    @Bind(R.id.text_view_email)
    TextView emailAddress;
    @Bind(R.id.text_view_phone_number)
    TextView phoneNumber;
    @Bind(R.id.text_view_complex)
    TextView complexName;
    @Bind(R.id.text_view_description)
    TextView description;
    @Bind(R.id.text_view_address)
    TextView address;
    @Bind(R.id.text_view_username)
    TextView username;
    private GoogleMap mGoogleMap;
    @Bind(R.id.layoutRate)
    LinearLayout layoutRate;
    @Bind(R.id.txtAverageRate)
    TextView txtAverageRate;
    private boolean isFollowed;
    private SupportMapFragment mapFragment;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {

            new Handler().post(new Runnable() {
                public void run() {
                    Activity activity = getActivity();
                    if (activity != null) {
                        SupportMapFragment fragment = (SupportMapFragment) (getChildFragmentManager().findFragmentById(R.id.map_view));
                        if (fragment != null) {
                            getChildFragmentManager().beginTransaction()
                                    .remove(fragment)
                                    .commitAllowingStateLoss();
                        }
                    }
                }
            });
        } catch (Exception ex) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_tab_info, container, false);
        Bundle args = getArguments();
        mShop = args.getParcelable("shop");
        ButterKnife.bind(this, view);
        HttpClient.get(String.format(Constants.Server.Shop.GET_SHOP, mShop.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (isVisible()) {
                    final Shop shop = GsonParser.getObjectFromGson(responseBody, Shop.class);
                    totalFollowers.setText(shop.getFollowers().size() + "");
                    if (shop.getFollowers() != null) {
                        follow.setText(shop.getFollowers()
                                .contains(Logged.Models.getUserProfile().getId()) ? getContext().getString(R.string.unfollow) : getContext()
                                .getString(R.string.follow));
                    } else {
                        follow.setText(getContext().getString(R.string.follow));
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

            }
        });
        isFollowed = mShop.getFollowers() != null && mShop.getFollowers().contains(Logged.Models.getUserProfile().getId()) ? true : false;
        complexName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, mShop.getComplex());
            }
        });
        totalFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("id", mShop.getId());
                bundle.putInt("flag", 2);
                FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);

            }
        });
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Prevent from multiple clicking on the view
                follow.setEnabled(false);

                if (!isFollowed) {
                    HttpClient.get(String.format(Constants.Server.Shop.GET_FOLLOW, mShop.getId()), new AsyncHttpResponser(getContext()) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            isFollowed = true;
                            follow.setText(R.string.unfollow);
                            totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                    .toString()) + 1));
                            follow.setEnabled(true);
                            mShop.getFollowers().add(Logged.Models.getUserProfile().getId());
                        }

                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers,
                                              byte[] responseBody,
                                              Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);

                            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_following), Toast.LENGTH_SHORT)
                                    .show();
                            follow.setEnabled(true);
                        }
                    });
                } else {
                    HttpClient.get(String.format(Constants.Server.Shop.GET_UNFOLLOW, mShop.getId()), new AsyncHttpResponser(getContext()) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            isFollowed = false;
                            follow.setText(R.string.follow);
                            totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                    .toString()) - 1));
                            follow.setEnabled(true);
                            mShop.getFollowers().remove(Logged.Models.getUserProfile().getId());
                        }

                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers,
                                              byte[] responseBody,
                                              Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);

                            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_unfollowing), Toast.LENGTH_SHORT)
                                    .show();
                            follow.setEnabled(true);
                        }
                    });
                }
            }
        });

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_view);
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
                if (latLng.latitude == 0 && latLng.longitude == 0) {
                } else {
                    mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(getContext().getString(R.string.placed_here)));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getContext()
                            .getResources()
                            .getInteger(R.integer.map_zoom_far)));
                }
                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng mapLatLng) {
                        Intent intent = new Intent(getActivity(), ActivityPickLocation.class);
                        intent.putExtra("location", latLng);
                        startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());

                    }
                });
            }
        });


        DrawableCompat.setTint(ratingBarShop.getProgressDrawable(), getContext().getResources()
                .getColor(R.color.colorAccent));

        // Set tags
        totalFollowers.setTag(mShop.getId());
        complexName.setTag(mShop.getComplex());

        if (!Utils.isNullOrEmpty(mShop.getRatesAverage())) {
            ratingBarShop.setRating(Float.valueOf(mShop.getRatesAverage()));
            txtAverageRate.setText(mShop.getRatesAverage().matches("[0-9]+\\.[0-9]*") ? mShop.getRatesAverage() : mShop.getRatesAverage() + ".0");
            totalRates.setText(Utils.getReadableCount(mShop.getRatesCount()));
        }
        setRatingColor(ratingBarShop, R.color.divider, R.color.rating_color);

        if (mShop.getFollowers() != null) {
            totalFollowers.setText(Utils.getReadableCount(mShop.getFollowers().size()));
        } else {
            totalFollowers.setText("0");
        }


        if (!Utils.isNullOrEmpty(mShop.getPhoneNumber())) {
            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setText(mShop.getPhoneNumber());
        }

        if (!Utils.isNullOrEmpty(mShop.getEmail())) {
            emailAddress.setVisibility(View.VISIBLE);
            emailAddress.setText(mShop.getEmail());
        }

        if (!Utils.isNullOrEmpty(mShop.getAddress())) {
            address.setVisibility(View.VISIBLE);
            address.setText(mShop.getAddress());
        }

        if (!Utils.isNullOrEmpty(mShop.getDescription())) {
            description.setVisibility(View.VISIBLE);
            description.setText(mShop.getDescription());
            description.setTransformationMethod(new LinkTransformationMethod(getActivity()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (!Utils.isNullOrEmpty(mShop.getUsername())) {
            username.setVisibility(View.VISIBLE);
            username.setText(mShop.getUsername());
        }

        if (mShop.getComplex() != null && !Utils.isNullOrEmpty(mShop.getComplex().getName())) {
            complexName.setVisibility(View.VISIBLE);
            complexName.setText(mShop.getComplex().getName());
        }


        if (mShop.getFollowers() != null) {
            follow.setText(mShop.getFollowers()
                    .contains(Logged.Models.getUserProfile().getId()) ? getContext().getString(R.string.unfollow) : getContext()
                    .getString(R.string.follow));
        } else {
            follow.setText(getContext().getString(R.string.follow));
        }
        layoutRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getContext());

                mSheetDialog.contentView(R.layout.bottom_sheet_rate)
                        .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .inDuration(300)
                        .cancelable(true)
                        .show();
                RatingBar bar = (RatingBar) mSheetDialog.findViewById(R.id.rating_bar);
                bar.setOnTouchListener(FragmentBusinessInfo.this);
                setRatingColor(bar, R.color.accent_opacity, R.color.colorAccent);

                bar.setOnRatingBarChangeListener(FragmentBusinessInfo.this);

            }
        });
        return view;
    }

    public void setRatingColor(RatingBar bar, int colorNormal, int colorFill) {
        LayerDrawable stars = (LayerDrawable) bar.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(ContextCompat.getColor(getContext(), colorNormal), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(ContextCompat.getColor(getContext(), colorFill), PorterDuff.Mode.SRC_ATOP);

    }


    @SuppressWarnings("unchecked")
    @Override
    public void onRatingChanged(final RatingBar ratingBar, float rating, boolean fromUser) {
        String url = String.format(Constants.Server.Shop.GET_RATE, mShop.getId(), Float.toString(rating));
        HttpClient.get(url, new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CheckOut checkOut = GsonParser.getObjectFromGson(responseBody, CheckOut.class);
                totalRates.setText(Integer.toString(checkOut.getId()));
                String averageVotes = checkOut.getValue();
                float value = Float.valueOf(averageVotes);
                ratingBarShop.setRating(value);
                mShop.setRatesAverage(checkOut.getValue());
                mShop.setRatesCount(checkOut.getId());
                txtAverageRate.setText(averageVotes.matches("[0-9]+\\.[0-9]*") ? averageVotes : averageVotes + ".0");
                setRatingColor(ratingBar, R.color.green_opacity, R.color.green);

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                Toast.makeText(getActivity(), getString(R.string.toast_error_updating_rate), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setRatingColor((RatingBar) v, R.color.accent_opacity, R.color.colorAccent);

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}