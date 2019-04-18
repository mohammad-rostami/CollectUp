package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.rey.material.app.SimpleDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/4/2016.
 */
@SuppressLint ("ValidFragment")

public class FragmnetTwoStepEnter extends BaseFragment {
  @Bind (R.id.edtPassword)
  EditText edtPassword;
  @Bind (R.id.txtDesc)
  TextView txtDesc;
  @Bind (R.id.btnDone)
  Button btnDone;
  @Bind (R.id.btnForgot)
  TextView btnForgot;
  public final static int MODE_RESET = 0;
  public final static int MODE_ENTER = 1;
  private View view;

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_twosteps);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);

  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {

      view = inflater.inflate(R.layout.fragment_enter_twostep, container, false);
      view.setFocusableInTouchMode(true);
      view.requestFocus();
      view.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK)
          {
            PrefFragmentTwoStep.forceClose = true;
            FragmentHandler.onBackPressed(getContext());
            return true;
          }
          return false;
        }
      });
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      btnForgot.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          final SimpleDialog builder = new SimpleDialog(getContext());
          builder.message(R.string.dialog_reset_pass_twostep)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.title_activity_reset_pass)
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
              HttpClient.get(String.format(Constants.Server.OAuth.GET_RESET_PASSWORD, Logged.Models.getUserProfile().getPhoneNumber()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  super.onSuccess(statusCode, headers, responseBody);
                  builder.dismiss();
                  final SimpleDialog dialogSuccess = new SimpleDialog(getContext());
                  dialogSuccess.message(R.string.message_success_forgotpass_twostep)
                    .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                    .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .positiveAction(R.string.ok)
                    .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .setCancelable(true);
                  dialogSuccess.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      dialogSuccess.dismiss();
                    }
                  });
                  dialogSuccess.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);
                  Toast.makeText(getContext(), R.string.toast_error_forgotpass_twostep, Toast.LENGTH_SHORT).show();
                }
              });

            }
          });

          builder.show();


        }
      });
      btnDone.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (!Utils.isNullOrEmpty(edtPassword.getText().toString()))
          {
            if (edtPassword.getText().toString().length() >= 4)
            {
              btnDone.setEnabled(false);
              HttpClient.get(String.format(Constants.Server.OAuth.GET_CHECK_PASSWORD, edtPassword.getText().toString()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  super.onSuccess(statusCode, headers, responseBody);
                  MyApplication.twoStepPass = edtPassword.getText().toString();
                  FragmentHandler.onBackPressed(getContext());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);
                  btnDone.setEnabled(true);
                  if (statusCode == 400)
                  {
                    Toast.makeText(getContext(), R.string.toast_error_enter_twostep, Toast.LENGTH_SHORT).show();
                  }


                }
              });
            } else
            {
              edtPassword.setError(getString(R.string.error_password_lenth));
            }
          } else
          {
            edtPassword.setError(getResources().getString(R.string.error_fill));
          }
        }
      });

      return view;
    } else
    {
      return view;
    }
  }


}
