/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.listeners.ILocationCallback;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

public class ActivityPickLocation extends BaseActivity
  implements GoogleMap.OnMapClickListener {

  private static LatLng mMarkerLatLng;
  private GoogleMap mMapFragment;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.UPDATE_SETTINGS.ordinal())
    {
      recreate();
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pick_location);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
    }


    final Bundle args = getIntent().getExtras();


    ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view)).getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final GoogleMap googleMap) {
        mMapFragment = googleMap;
        mMapFragment.getUiSettings().setZoomControlsEnabled(true);

        final Activity _activity = ActivityPickLocation.this;
        PermissionListener dialogPermissionListener =
          CustomPermissionDialog.Builder
            .withContext(_activity)
            .withTitle(R.string.permission_title)
            .withMessage(R.string.permission_gps)
            .withButtonText(android.R.string.ok)
            .build();
        PermissionListener basePermission = new PermissionListener() {
          @Override
          public void onPermissionGranted(PermissionGrantedResponse response) {
            mMapFragment.setMyLocationEnabled(true);
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
              .addApi(LocationServices.API).build();
            //   .addConnectionCallbacks(this)
            // .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
              .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
              LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
              @Override
              public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode())
                {
                  case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                  case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try
                    {
                      // Show the dialog by calling startResolutionForResult(),
                      // and check the result in onActivityResult().
                      status.startResolutionForResult(
                        ActivityPickLocation.this, 1000);
                    } catch (IntentSender.SendIntentException e)
                    {
                      // Ignore the error.
                    }
                    break;
                  case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
                }
              }
            });
          }

          @Override
          public void onPermissionDenied(PermissionDeniedResponse response) {

          }

          @Override
          public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();

          }
        };
        PermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
        Dexter.withActivity(ActivityPickLocation.this)
          .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
          .withListener(compositePermissionListener)
          .check();


        mMapFragment.getUiSettings().setMyLocationButtonEnabled(true);
        mMapFragment.getUiSettings().setCompassEnabled(false);
        mMapFragment.getUiSettings().setMapToolbarEnabled(false);

        if (args.getBoolean("return"))
        {
          mMapFragment.setOnMapClickListener(ActivityPickLocation.this);
        }

        if (args.getParcelable("location") == null)
        {
          new Location(ActivityPickLocation.this).getLocation(new ILocationCallback() {
            @Override
            public void onLocationFound(LatLng latLng) {
              mMapFragment.addMarker(new MarkerOptions().position(latLng)
                .title(getString(R.string.placed_here)));
              mMapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), getResources()
                .getInteger(R.integer.map_zoom_near)));
              mMarkerLatLng = latLng;
            }

            @Override
            public void onCouldNotGetLocation() {

            }
          });
        } else
        {
          LatLng argLocation = args.getParcelable("location");
          if (argLocation.latitude != 0 && argLocation.longitude != 0)
          {
            mMapFragment.addMarker(new MarkerOptions().position(argLocation)
              .title(getString(R.string.placed_here)));
            mMapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(argLocation, getResources()
              .getInteger(R.integer.map_zoom_near)));
          }
        }
      }
    });

    if (!args.getBoolean("return"))
    {
      if (actionBar != null)
      {
        actionBar.setTitle(getString(R.string.display_location));
      }
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_pick_location, menu);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (getIntent() == null || !getIntent().getBooleanExtra("return", false))
    {
      menu.findItem(R.id.action_done).setVisible(false);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_done:
        if (mMarkerLatLng == null)
        {
          mMarkerLatLng = new LatLng(0, 0);
        }

        try
        {
          Intent intent = new Intent();
          intent.putExtra("location", mMarkerLatLng);
          setResult(RESULT_OK, intent);
          finish();
        } catch (Exception e)
        {
          Toast.makeText(ActivityPickLocation.this, e.getMessage(), Toast.LENGTH_LONG)
            .show();
        }


        break;
      case android.R.id.home:
        onBackPressed();
        break;
    }

    return false;
  }

  @Override
  public void onMapClick(final LatLng latLng) {
    mMapFragment.clear();

    mMarkerLatLng = latLng;

    mMapFragment.addMarker(new MarkerOptions().position(latLng));
  }

  @Override
  public void onMenuNotificationsClick() {

  }

  @Override
  public void onMenuTimelineClick() {

  }

  @Override
  public void onMenuShopClick() {

  }

  @Override
  public void onConversationClick() {

  }

}
