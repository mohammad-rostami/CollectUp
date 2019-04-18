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
import com.collect_up.c_up.model.Complex;
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
public class FragmentComplexInfo extends Fragment implements RatingBar.OnRatingBarChangeListener, RatingBar.OnTouchListener {
    @Bind(R.id.text_view_votes)
    TextView totalRates;
    @Bind(R.id.text_view_total_followers)
    TextView totalFollowers;
    @Bind(R.id.rating_bar)
    RatingBar ratingBar;
    @Bind(R.id.button_follow)
    Button follow;
    @Bind(R.id.text_view_email)
    TextView emailAddress;
    @Bind(R.id.text_view_phone_number)
    TextView phoneNumber;
    @Bind(R.id.text_view_description)
    TextView description;
    @Bind(R.id.text_view_username)
    TextView username;
    @Bind(R.id.text_view_address)
    TextView address;
    private GoogleMap mGoogleMap;
    @Bind(R.id.layoutRate)
    LinearLayout layoutRate;
    @Bind(R.id.txtAverageRate)
    TextView txtAverageRate;
    private Complex mComplex;
    private boolean isFollowed;
    private SupportMapFragment mapFragment;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complex_tab_info, container, false);
        Bundle args = getArguments();
        mComplex = args.getParcelable("complex");
        ButterKnife.bind(this, view);

        isFollowed = mComplex.getFollowers() != null && mComplex.getFollowers().contains(Logged.Models.getUserProfile().getId()) ? true : false;
        totalFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("id", mComplex.getId());
                bundle.putInt("flag", 3);
                FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);
            }
        });
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Prevent from multiple clicking on the view
                follow.setEnabled(false);

                if (!isFollowed) {
                    HttpClient.get(String.format(Constants.Server.Complex.GET_FOLLOW, mComplex.getId()), new AsyncHttpResponser(getContext()) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            isFollowed = true;
                            follow.setText(R.string.unfollow);
                            totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                    .toString()) + 1));
                            follow.setEnabled(true);
                            mComplex.getFollowers().add(Logged.Models.getUserProfile().getId());
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
                    HttpClient.get(String.format(Constants.Server.Complex.GET_UNFOLLOW, mComplex.getId()), new AsyncHttpResponser(getContext()) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            isFollowed = false;
                            follow.setText(R.string.follow);
                            totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                    .toString()) - 1));
                            follow.setEnabled(true);
                            mComplex.getFollowers().remove(Logged.Models.getUserProfile().getId());
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

                final LatLng latLng = new LatLng(Double.valueOf(mComplex.getLat()), Double.valueOf(mComplex.getLong()));
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


        DrawableCompat.setTint(ratingBar.getProgressDrawable(), getContext().getResources()
                .getColor(R.color.colorAccent));

        // Set tags
        totalFollowers.setTag(mComplex.getId());

        if (!Utils.isNullOrEmpty(mComplex.getRatesAverage())) {
            ratingBar.setRating(Float.valueOf(mComplex.getRatesAverage()));
            txtAverageRate.setText(mComplex.getRatesAverage().matches("[0-9]+\\.[0-9]*") ? mComplex.getRatesAverage() : mComplex.getRatesAverage() + ".0");
            totalRates.setText(Utils.getReadableCount(mComplex.getRatesCount()));
        }
        setRatingColor(ratingBar, R.color.divider, R.color.rating_color);

        if (mComplex.getFollowers() != null) {
            totalFollowers.setText(Utils.getReadableCount(mComplex.getFollowers().size()));
        } else {
            totalFollowers.setText("0");
        }


        if (!Utils.isNullOrEmpty(mComplex.getPhoneNumber())) {
            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setText(mComplex.getPhoneNumber());
        }

        if (!Utils.isNullOrEmpty(mComplex.getEmail())) {
            emailAddress.setVisibility(View.VISIBLE);
            emailAddress.setText(mComplex.getEmail());
        }

        if (!Utils.isNullOrEmpty(mComplex.getAddress())) {
            address.setVisibility(View.VISIBLE);
            address.setText(mComplex.getAddress());
        }

        if (!Utils.isNullOrEmpty(mComplex.getDescription())) {
            description.setVisibility(View.VISIBLE);
            description.setText(mComplex.getDescription());
            description.setTransformationMethod(new LinkTransformationMethod(getActivity()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (!Utils.isNullOrEmpty(mComplex.getUsername())) {
            username.setVisibility(View.VISIBLE);
            username.setText(mComplex.getUsername());
        }


        if (mComplex.getFollowers() != null) {
            follow.setText(mComplex.getFollowers()
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
                bar.setOnTouchListener(FragmentComplexInfo.this);
                setRatingColor(bar, R.color.accent_opacity, R.color.colorAccent);

                bar.setOnRatingBarChangeListener(FragmentComplexInfo.this);

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
    public void onRatingChanged(final RatingBar rateBar, float rating, boolean fromUser) {
        String url = String.format(Constants.Server.Complex.GET_RATE, mComplex.getId(), Float.toString(rating));
        HttpClient.get(url, new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CheckOut checkOut = GsonParser.getObjectFromGson(responseBody, CheckOut.class);
                totalRates.setText(Integer.toString(checkOut.getId()));
                String averageVotes = checkOut.getValue();
                float value = Float.valueOf(averageVotes);
                ratingBar.setRating(value);
                mComplex.setRatesAverage(checkOut.getValue());
                mComplex.setRatesCount(checkOut.getId());
                txtAverageRate.setText(averageVotes.matches("[0-9]+\\.[0-9]*") ? averageVotes : averageVotes + ".0");
                setRatingColor(rateBar, R.color.green_opacity, R.color.green);

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

}
