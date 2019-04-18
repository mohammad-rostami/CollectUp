/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.collect_up.c_up.model.Profile;

import java.util.Comparator;

/**
 * Created by Collect-up  on 10/22/2015.
 */
public class SortProfiles implements Comparator<Profile> {
  @Override
  public int compare(Profile lhs, Profile rhs) {
    int number = 0;
    try
    {
      number = lhs.getName().compareToIgnoreCase(rhs.getName());
    } catch (Exception ex)
    {
    }
    return number;
  }
}
