/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.ILocationCallback;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.receivers.LocationReceiver;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Location extends Service implements LocationListener {
  // The minimum distance to change Updates in meters
  private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
  // The minimum time between updates in milliseconds
  private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
  // Declaring a Location Manager
  protected LocationManager locationManager;
  // flag for GPS status
  boolean isGPSEnabled = false;
  // flag for network status
  boolean isNetworkEnabled = false;
  // flag for GPS status
  boolean canGetLocation = false;
  android.location.Location location; // location
  private Context mContext;
  private LocationReceiver broadcastReceiver = new LocationReceiver();

  public Location(Context context) {
    this.mContext = context;
  }

  public Location() {
  }

  public static String getCountryAndCity(Context context, LatLng latLng) {
    try
    {
      Geocoder geocoder;
      List<Address> addresses;
      geocoder = new Geocoder(context, Locale.getDefault());

      addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

      if (addresses.size() > 0)
      {
        String city = addresses.get(0).getLocality();
        String country = addresses.get(0).getCountryName();

        if (Utils.isNullOrEmpty(city))
        {
          return country;
        }
        return country + ", " + city;
      }

    } catch (IOException ignored)
    {

    }

    return null;
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(broadcastReceiver);
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    this.mContext = getApplicationContext();
    getLocation();
    return super.onStartCommand(intent, flags, startId);
  }

  private void getLocation() {
    try
    {
      locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

      // getting GPS status
      isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

      this.canGetLocation = true;
      // First get location from Network Provider
      if (isNetworkEnabled)
      {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      }
      // if GPS Enabled get lat/long using GPS Services
      if (location == null)
      {
        if (isGPSEnabled)
        {
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
          location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
      }

    } catch (Exception ignored)
    {

    }

    if (location != null)
    {
      Profile profile = Logged.Models.getUserProfile();

      profile.setLat(Double.toString(location.getLatitude()));
      profile.setLong(Double.toString(location.getLongitude()));

      Hawk.put("profile", profile);

      Intent intent = new Intent(mContext, LocationReceiver.class);
      intent.putExtra("locationFound", true);
      intent.putExtra("latLng", new LatLng(location.getLatitude(), location.getLongitude()));
      mContext.sendBroadcast(intent);
    } else
    {
      Intent intent = new Intent(mContext, LocationReceiver.class);
      intent.putExtra("locationFound", false);
      mContext.sendBroadcast(intent);
    }

    stopSelf();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    registerReceiver(broadcastReceiver, new IntentFilter());
  }

  public void getLocation(final ILocationCallback callback) {
    try
    {
      locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

      // getting GPS status
      isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

      // First get location from Network Provider
      if (isNetworkEnabled)
      {
        this.canGetLocation = true;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null)
        {
          callback.onLocationFound(new LatLng(location.getLatitude(), location.getLongitude()));
        }
      }
      // if GPS Enabled get lat/long using GPS Services
      if (location == null)
      {
        if (isGPSEnabled)
        {
          this.canGetLocation = true;
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
          location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
          if (location != null)
          {
            callback.onLocationFound(new LatLng(location.getLatitude(), location.getLongitude()));
          }
        }
      }

      if (location == null)
      {
        callback.onCouldNotGetLocation();
      }

    } catch (Exception ignored)
    {

    }
  }

  /**
   * Stop using GPS listener
   * Calling this function will stop using GPS in your app
   */
  public void stopUsingGPS() {
    if (locationManager != null)
    {
      locationManager.removeUpdates(this);
    }
  }

  @Override
  public void onLocationChanged(android.location.Location location) {
  }

  @Override
  public void onProviderDisabled(String provider) {
  }

  @Override
  public void onProviderEnabled(String provider) {
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

}