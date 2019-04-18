/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class Filter {
  public String SearchText;
  /*
  0: no filter
  1: filter by distance
  2: filter by top rate
  3: filter by distance and top rate
   */
  public int SearchOption;
  public double Latitude;
  public double Longitude;

  public String getSearchText() {
    return SearchText;
  }

  public void setSearchText(String searchText) {
    SearchText = searchText;
  }

  public int getSearchOption() {
    return SearchOption;
  }

  public void setSearchOption(int searchOption) {
    SearchOption = searchOption;
  }

  public double getLatitude() {
    return Latitude;
  }

  public void setLatitude(double latitude) {
    Latitude = latitude;
  }

  public double getLongitude() {
    return Longitude;
  }

  public void setLongitude(double longitude) {
    Longitude = longitude;
  }
}
