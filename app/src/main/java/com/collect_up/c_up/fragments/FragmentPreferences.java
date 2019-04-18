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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.PreferenceHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;

import butterknife.Bind;

public class FragmentPreferences extends PreferenceFragmentCompat
  implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static boolean isRunning;
  @Bind (R.id.recyclerPrefrences)
  RecyclerView mList;
  private AppCompatDelegate mDelegate;
  private View view;

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);

  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.preferences);
    PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    setDefaultsDynamically();

    findPreference("pref_manage_notifications").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_NOTIFS, null);
        return false;
      }
    });
    findPreference("pref_blocked_list").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_BLOCKLIST, null);
        return false;
      }
    });
    findPreference("pref_denied_profiles").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_DENIEDLIST, null);
        return false;
      }
    });
    findPreference("stickers").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_MANAGE_STICKER, null);
        return false;
      }
    });
    findPreference("pref_twosteps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (Utils.isNullOrEmpty(MyApplication.twoStepPass))
        {
          FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_TWOSTEP, false);
        }
        return false;
      }
    });
    findPreference("pref_sessions").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_SESSIONMANAGER, null);
        return false;
      }
    });
    findPreference("pref_privacy").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_PRIVACY, null);
        return false;
      }
    });

  }

  private void setDefaultsDynamically() {
    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

    prefs.putBoolean("prefAccountIsPrivate", Logged.Models.getUserProfile().getIsPrivate());
    prefs.putBoolean("prefPrivateMessaging", Logged.Models.getUserProfile().getPrivateMessaging());

    prefs.apply();
  }


  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
    onDestroy();
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  @Override
  public void onPause() {
    super.onPause();
    onDestroy();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    DrawerHelper.getDrawer().setSelection(-1);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals("prefAccountIsPrivate"))
    {
      HttpClient.get(String.format(Constants.Server.Profile.GET_CHANGE_PRIVATE, PreferenceHelper.getBoolean(getActivity().getApplicationContext(), "prefAccountIsPrivate")), new AsyncHttpResponser(getActivity()) {
        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
          Logged.Models.getUserProfile().setIsPrivate(PreferenceHelper.getBoolean(getActivity().getApplicationContext(), "prefAccountIsPrivate"));
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

        }
      });
    } else if (key.equals("prefPrivateMessaging"))
    {
      HttpClient.get(String.format(Constants.Server.Profile.GET_CHANGE_PRIVATE_MESSAGING, PreferenceHelper.getBoolean(getActivity().getApplicationContext(), "prefPrivateMessaging")), new AsyncHttpResponser(getActivity()) {
        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
          Logged.Models.getUserProfile().setPrivateMessaging(PreferenceHelper.getBoolean(getActivity().getApplicationContext(), "prefPrivateMessaging"));
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

        }
      });
    }
  }


}
