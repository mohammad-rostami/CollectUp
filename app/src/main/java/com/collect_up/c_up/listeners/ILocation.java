/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Collect-up  on 11/23/2015.
 */
public interface ILocation {
  void LocationFound(LatLng latLng);

  void LocationNotFound();
}
