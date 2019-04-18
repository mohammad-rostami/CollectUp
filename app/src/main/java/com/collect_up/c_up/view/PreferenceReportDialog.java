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
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.CheckOut;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import cz.msebera.android.httpclient.Header;

public class PreferenceReportDialog extends Preference {

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PreferenceReportDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public PreferenceReportDialog(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public PreferenceReportDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PreferenceReportDialog(Context context) {
    super(context);
  }

  private com.rey.material.app.Dialog buildDialog() {

    final com.rey.material.app.Dialog mDialogBuilder = new com.rey.material.app.Dialog(getContext());
    final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);

    mDialogBuilder//.message(R.string.write_problem_for_report)
    //  .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.pref_report_a_problem)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.action_report)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .setCancelable(true);
    mDialogBuilder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialogBuilder.dismiss();

      }
    });
    mDialogBuilder.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
     mDialogBuilder.setContentView(view);


    mDialogBuilder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EditText reportEditText = (EditText) view.findViewById(R.id.edit_text_report_message);
        final String reportText = reportEditText.getText().toString().trim();

        if (reportText.length() > 0) {
          CheckOut checkOut = new CheckOut();
          checkOut.setValue(reportText);
          HttpClient.post(getContext(), String.format(Constants.Server.Problem.POST_REPORT, Hawk.get("profileId")), new Gson().toJson(checkOut, CheckOut.class), "application/json", new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              Utils.displayToast(getContext(), getContext().getString(R.string.toast_reporting_successfully), Gravity.CENTER, Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

              Utils.displayToast(getContext(), getContext().getString(R.string.toast_reporting_problem_failed), Gravity.CENTER, Toast.LENGTH_SHORT);
            }
          });
        }
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
