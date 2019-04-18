/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.collect_up.c_up.listeners.ILocation;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Collect-up  on 11/23/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

  private static ILocation mCallback;

  public LocationReceiver() {
  }

  public LocationReceiver(ILocation callback) {
    mCallback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (mCallback != null)
    {
      if (intent.getBooleanExtra("locationFound", false))
      {
        mCallback.LocationFound((LatLng) intent.getParcelableExtra("latLng"));

      } else
      {
        mCallback.LocationNotFound();
      }
    }

  }
}
