/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;


public class PrefFragmentManageNotifications extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static boolean isRunning;


  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.manage_notification_preferences);
    PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);


  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.notification_settings);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }


  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

  }
}
