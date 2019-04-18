/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.model.Session;
import com.collect_up.c_up.view.SessionPreference;

public class PrefFragmentSessionManager extends PreferenceFragmentCompat {
    public static boolean isRunning;
    private AppCompatDelegate mDelegate;
    private Session[] sessions;
    private View view;


    @Override
    public void onResume() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_session_manager);
        ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.session_manager_preferences);
        HttpClient.get(Constants.Server.OAuth.SESSIONS, new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                sessions = GsonParser.getArrayFromGson(responseBody, Session[].class);
                if (sessions != null) {
                    final PreferenceCategory prefCatActiveSession = (PreferenceCategory) findPreference("activeSession");
                    final PreferenceCategory prefCatCurrentSession = (PreferenceCategory) findPreference("currentSession");

                    for (int i = 0; i < sessions.length; i++) {

                        final String sessionId = sessions[i].getId();
                        final SessionPreference pref = new SessionPreference(getContext());
                        pref.setDeviceName(sessions[i].getDevice());
                        pref.setIpAddress(sessions[i].getIpAddress());
                        pref.setPlatform(sessions[i].getPaltform());

                        if (sessions[i].isOnline()) {
                            pref.setLastOnline("online");
                        } else {
                            pref.setLastOnline(TimeHelper.getChatTimeAgo(getContext(),
                                    TimeHelper.getDateFromServerDatePattern(getContext(),
                                            sessions[i].getLastOnline()),
                                    true));
                        }

                        if (!sessions[i].isActiveSession()) {
                            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                                @Override
                                public boolean onPreferenceClick(Preference preference) {

                                    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
                                    builder.message(R.string.terminate_session)
                                            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                                            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                                            .positiveAction(R.string.terminate)
                                            .negativeAction(R.string.cancel)
                                            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                                            .setCancelable(true);

                                    builder.negativeActionClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            builder.dismiss();

                                        }
                                    });
                                    builder.positiveActionClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            HttpClient.post(getContext(),
                                                    String.format(Constants.Server.OAuth.TERMINATE, sessionId),
                                                    "application/json", new AsyncHttpResponser(getContext()) {

                                                        @Override
                                                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                                                            super.onSuccess(statusCode, headers, responseBody);
                                                            prefCatActiveSession.removePreference(pref);
                                                            builder.dismiss();

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
                        }
                        if (!sessions[i].isActiveSession()) {
                            prefCatActiveSession.addPreference(pref);
                        } else {
                            prefCatCurrentSession.addPreference(pref);
                        }

                    }
                    final Preference prefTerminateAll = new Preference(getContext());
                    prefTerminateAll.setTitle(R.string.terminate_all);
                    prefCatCurrentSession.addPreference(prefTerminateAll);
                    prefTerminateAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            HttpClient.post(getContext(), Constants.Server.OAuth.TERMINATE_ALL, "application/json", new AsyncHttpResponser(getContext()) {
                                @Override
                                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                                    super.onSuccess(statusCode, headers, responseBody);
                                    prefCatActiveSession.removeAll();
                                }

                                @Override
                                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                                    super.onFailure(statusCode, headers, responseBody, error);
                                }
                            });
                            return false;
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
        }
        return super.onOptionsItemSelected(item);
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


}
