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
import android.support.v7.preference.PreferenceFragmentCompat;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;

public class PrefFragmentAccountPrivacy extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static boolean isRunning;

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_account_privacy);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }


  private void setDefaultsDynamically() {
    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

    prefs.putBoolean("prefShowUsername", Logged.Models.getUserProfile().getSettingsUsername());
    prefs.putBoolean("prefShowGender", Logged.Models.getUserProfile().getSettingsGender());
    prefs.putBoolean("prefShowJob", Logged.Models.getUserProfile().getSettingsJob());
    prefs.putBoolean("prefShowEmail", Logged.Models.getUserProfile().getSettingsEmail());
    prefs.putBoolean("prefShowPhoneNumber", Logged.Models.getUserProfile().getSettingsPhoneNumber());
    prefs.putBoolean("prefShowBirthday", Logged.Models.getUserProfile().getSettingsBirthday());
    prefs.putBoolean("prefShowLocation", Logged.Models.getUserProfile().getSettingsLocation());
    prefs.putBoolean("prefShowLanguages", Logged.Models.getUserProfile().getSettingsLanguages());
    prefs.putBoolean("prefShowBiography", Logged.Models.getUserProfile().getSettingsBiography());

    prefs.apply();
  }


  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.account_privacy_preferences);
    PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    setDefaultsDynamically();


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
  public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
    switch (key)
    {
      case "prefShowUsername":
        final boolean newUsernameValue = sharedPreferences.getBoolean("prefShowUsername", Logged.Models.getUserProfile().getSettingsUsername());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_USERNAME, newUsernameValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsUsername(newUsernameValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowGender":
        final boolean newGenderValue = sharedPreferences.getBoolean("prefShowGender", Logged.Models.getUserProfile().getSettingsGender());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_GENDER, newGenderValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsGender(newGenderValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowJob":
        final boolean newJobValue = sharedPreferences.getBoolean("prefShowJob", Logged.Models.getUserProfile().getSettingsJob());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_JOB, newJobValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsJob(newJobValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowEmail":
        final boolean newEmailValue = sharedPreferences.getBoolean("prefShowEmail", Logged.Models.getUserProfile().getSettingsEmail());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_EMAIL, newEmailValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsEmail(newEmailValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowPhoneNumber":
        final boolean newPhoneNumberValue = sharedPreferences.getBoolean("prefShowPhoneNumber", Logged.Models.getUserProfile().getSettingsPhoneNumber());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_PHONE_NUMBER, newPhoneNumberValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsPhoneNumber(newPhoneNumberValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowBirthday":
        final boolean newBirthdayValue = sharedPreferences.getBoolean("prefShowBirthday", Logged.Models.getUserProfile().getSettingsBirthday());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_BIRTHDAY, newBirthdayValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsBirthday(newBirthdayValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowLocation":
        final boolean newLocationValue = sharedPreferences.getBoolean("prefShowLocation", Logged.Models.getUserProfile().getSettingsLocation());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_LOCATION, newLocationValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsLocation(newLocationValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowLanguages":
        final boolean newLanguagesValue = sharedPreferences.getBoolean("prefShowLanguages", Logged.Models.getUserProfile().getSettingsLanguages());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_LANGUAGES, newLanguagesValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsLanguages(newLanguagesValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
      case "prefShowBiography":
        final boolean newBiographyValue = sharedPreferences.getBoolean("prefShowBiography", Logged.Models.getUserProfile().getSettingsBiography());

        HttpClient.get(String.format(Constants.Server.Preferences.GET_BIOGRAPHY, newBiographyValue), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
            Logged.Models.getUserProfile().setSettingsBiography(newBiographyValue);
          }

          @Override
          public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

          }
        });
        break;
    }
  }
}
