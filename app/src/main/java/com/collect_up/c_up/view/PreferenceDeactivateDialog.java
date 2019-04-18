/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Utils;

import cz.msebera.android.httpclient.Header;

public class PreferenceDeactivateDialog extends Preference {

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PreferenceDeactivateDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public PreferenceDeactivateDialog(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public PreferenceDeactivateDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PreferenceDeactivateDialog(Context context) {
    super(context);
  }

  private com.rey.material.app.SimpleDialog buildDialog() {
    final com.rey.material.app.SimpleDialog mDialogBuilder = new com.rey.material.app.SimpleDialog(getContext());
    mDialogBuilder.message(R.string.sure_to_deactivate)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.pref_deactivate_account)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.action_deactivate)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .setCancelable(true);

    mDialogBuilder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialogBuilder.dismiss();

      }
    });

    mDialogBuilder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        HttpClient.get(Constants.Server.Profile.GET_DEACTIVATE, new AsyncHttpResponser(getContext(), Looper.myLooper()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            super.onSuccess(statusCode, headers, responseBody);
            Utils.clearAppData(getContext());

          }

          @Override
          public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
          }
        });
      }
    });

    return mDialogBuilder;
  }

  @Override
  protected void onClick() {
    super.onClick();
    buildDialog().show();
  }
}
