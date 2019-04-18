package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.view.NonClickableSummaryPreference;
import com.rey.material.app.SimpleDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by collect-up3 on 9/1/2016.
 */
@SuppressLint ("ValidFragment")

public class PrefFragmentTwoStep extends PreferenceFragmentCompat {
  public static boolean isRunning;
  private AppCompatDelegate mDelegate;
  public static boolean forceClose;


  public PrefFragmentTwoStep(boolean forceClose) {
    this.forceClose = forceClose;
  }

  public PrefFragmentTwoStep() {
  }

  @Override

  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    addPreferencesFromResource(R.xml.two_step_preferences);

  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_twosteps);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
    if (forceClose)
    {
      FragmentHandler.onBackPressed(getContext());
      forceClose = false;
      return;
    }
    checkState();
  }

  private void checkState() {
    HttpClient.get(String.format(Constants.Server.OAuth.GET_SECURITY_STATE, Logged.Models.getUserProfile().getPhoneNumber()), new JsonHttpResponser(getContext()) {
        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
          super.onFailure(statusCode, headers, responseString, throwable);
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
          super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
          super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
          super.onSuccess(statusCode, headers, response);
          try
          {


            boolean twoStepEnabled = response.getBoolean("TwoStepVerificationEnabled");
            boolean emailVerified = response.getBoolean("EmailVerified");
            boolean isEmailSet = response.getBoolean("HasEmail");
            PreferenceScreen screen = getPreferenceScreen();
            screen.removeAll();
            if (!twoStepEnabled && !emailVerified && !isEmailSet)
            {


              Preference prefs = new Preference(getContext());
              prefs.setTitle(R.string.btn_enable_two_step);
              prefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  FragmentHandler.replaceFragment(getContext(), fragmentType.TWOSTEP_ENABLE, FragmentTwoStepEnable.MODE_EMAIL);

                  return false;

                }
              });
              prefs.setSummary(R.string.desc_two_step);
              screen.addPreference(prefs);

            } else if (isEmailSet && !emailVerified) //doesnt call anyway
            {
              //verifing
              NonClickableSummaryPreference notVerifyPref = new NonClickableSummaryPreference(getContext());
              notVerifyPref.setSummary(R.string.not_verified_additional_password_settings);
              Preference prefsChangeEmail = new Preference(getContext());
              prefsChangeEmail.setTitle(R.string.btn_change_eamil_two_step);
              prefsChangeEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  FragmentHandler.replaceFragment(getContext(), fragmentType.TWOSTEP_ENABLE, FragmentTwoStepEnable.MODE_EMAIL);
                  return false;
                }
              });

              screen.addPreference(prefsChangeEmail);

            } else if (isEmailSet && emailVerified && !twoStepEnabled)
            {
              //setPass
              Preference prefs = new Preference(getContext());
              prefs.setTitle(R.string.btn_set_password_two_step);
              prefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  FragmentHandler.replaceFragment(getContext(), fragmentType.TWOSTEP_ENABLE, FragmentTwoStepEnable.MODE_PASSWORD);


                  return false;
                }
              });
              prefs.setSummary(R.string.desc_two_step);
              screen.addPreference(prefs);
            } else
            {
              if (Utils.isNullOrEmpty(MyApplication.twoStepPass))
              {
                FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_TWOSTEP_ENTER, null);
              }

              //two-setp enabled
              Preference prefChangePass = new Preference(getContext());
              prefChangePass.setTitle(R.string.btn_change_password_two_step);
              prefChangePass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  FragmentHandler.replaceFragment(getContext(), fragmentType.TWOSTEP_ENABLE, FragmentTwoStepEnable.MODE_CHANGE_PASSWORD);


                  return false;
                }
              });
              Preference prefChangeEmail = new Preference(getContext());
              prefChangeEmail.setTitle(R.string.btn_change_eamil_two_step);
              prefChangeEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  FragmentHandler.replaceFragment(getContext(), fragmentType.TWOSTEP_ENABLE, FragmentTwoStepEnable.MODE_EMAIL);


                  return false;
                }
              });
              Preference prefDisableTwoStep = new Preference(getContext());
              prefDisableTwoStep.setTitle(R.string.btn_disable_two_step);
              prefDisableTwoStep.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                  final SimpleDialog builder = new SimpleDialog(getContext());
                  builder.message(R.string.dialog_disable_twostep)
                    .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                    .title(R.string.title_disable_twostep)
                    .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .positiveAction(R.string.alert_positive_pick_location)
                    .negativeAction(R.string.alert_negetive_pick_location)
                    .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .cancelable(false);
                  builder.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      builder.dismiss();
                    }
                  });
                  builder.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      HttpClient.get(String.format(Constants.Server.OAuth.GET_DISABLE_TWOSTEP, MyApplication.twoStepPass), new AsyncHttpResponser(getContext()) {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                          super.onSuccess(statusCode, headers, responseBody);
                          builder.dismiss();
                          checkState();

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                          super.onFailure(statusCode, headers, responseBody, error);
                        }
                      });

                    }
                  });

                  builder.show();
                  return false;
                }
              });
              screen.addPreference(prefChangePass);
              screen.addPreference(prefChangeEmail);
              screen.addPreference(prefDisableTwoStep);

            }

          } catch (JSONException e)
          {
            e.printStackTrace();
          }

        }
      }

    );
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
    MyApplication.twoStepPass = "";
  }


}