package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Session;
import com.rey.material.app.SimpleDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/4/2016.
 */
@SuppressLint ("ValidFragment")

public class FragmentTwoStepEnable extends BaseFragment {
  int mode;
  public final static int MODE_EMAIL = 0;
  public final static int MODE_PASSWORD = 1;
  public final static int MODE_CHANGE_PASSWORD = 2;
  public static boolean isRunning;
  private AppCompatDelegate mDelegate;
  private Session[] sessions;
  @Bind (R.id.edtRetypePassword)
  EditText edtRetypePassword;
  @Bind (R.id.edtPassword)
  EditText edtPassword;
  @Bind (R.id.edtEmail)
  EditText edtEmail;
  @Bind (R.id.txtDesc)
  TextView txtDesc;
  @Bind (R.id.btnEnable)
  Button btnEnable;
  private View view;

  public FragmentTwoStepEnable(int mode) {
    this.mode = mode;
  }

  public FragmentTwoStepEnable() {
  }

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);

    switch (mode)
    {

      case MODE_EMAIL:
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.hint_email_address);


        break;
      case MODE_PASSWORD:

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_twosteps);

        break;
      case MODE_CHANGE_PASSWORD:
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_change_pass);

        break;
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_enable_twostep, container, false);
      ButterKnife.bind(this, view);
      switch (mode)
      {

        case MODE_EMAIL:
          ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.hint_email_address);

          edtPassword.setVisibility(View.GONE);
          edtRetypePassword.setVisibility(View.GONE);
          txtDesc.setText(getResources().getString(R.string.desc_set_email_twostep));
          btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (SepehrUtil.isEmailValid(edtEmail.getText().toString()))
              {
                btnEnable.setEnabled(false);
                HttpClient.get(String.format(Constants.Server.OAuth.GET_SET_EMAIL, edtEmail.getText().toString()), new AsyncHttpResponser(getContext()) {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    super.onSuccess(statusCode, headers, responseBody);
                    FragmentHandler.onBackPressed(getContext());

                  }

                  @Override
                  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);
                    Toast.makeText(getContext(), R.string.toast_set_email_twostep_failed, Toast.LENGTH_SHORT).show();

                    btnEnable.setEnabled(true);

                  }
                });

              } else
              {
                edtEmail.setError(getResources().getString(R.string.error_email_validator));
              }
            }
          });
          break;
        case MODE_PASSWORD:

          ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_twosteps);

          btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if (!Utils.isNullOrEmpty(edtPassword.getText().toString()) && !Utils.isNullOrEmpty(edtRetypePassword.getText().toString()))
              {

                if (edtPassword.getText().toString().length() >= 4)
                {
                  if (edtPassword.getText().toString().equals(edtRetypePassword.getText().toString()))
                  {
                    btnEnable.setEnabled(false);

                    HttpClient.get(String.format(Constants.Server.OAuth.GET_SET_PASSWORD, edtPassword.getText().toString()), new AsyncHttpResponser(getContext()) {
                      @Override
                      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        super.onSuccess(statusCode, headers, responseBody);

                        final SimpleDialog dialogSuccess = new SimpleDialog(getContext());
                        dialogSuccess.message(R.string.message_success_setpass_twostep)
                          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                          .positiveAction(R.string.ok)
                          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                          .setCancelable(true);
                        dialogSuccess.positiveActionClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                            dialogSuccess.dismiss();
                            FragmentHandler.onBackPressed(getContext());

                          }
                        });
                        dialogSuccess.show();
                      }

                      @Override
                      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                        btnEnable.setEnabled(true);
                        Toast.makeText(getContext(), R.string.toast_set_password_twostep_failed, Toast.LENGTH_SHORT).show();
                      }
                    });
                  } else
                  {
                    edtRetypePassword.setError(getResources().getString(R.string.error_pass_notmatch));
                  }
                } else
                {
                  edtPassword.setError(getString(R.string.error_password_lenth));
                }


              } else

              {
                Toast.makeText(getContext(), R.string.error_fill, Toast.LENGTH_SHORT).show();
              }

            }
          });
          edtEmail.setVisibility(View.GONE);
          txtDesc.setText(getResources().getString(R.string.desc_set_password_twostep));

          break;
        case MODE_CHANGE_PASSWORD:
          ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_change_pass);
          btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if (!Utils.isNullOrEmpty(edtPassword.getText().toString()) && !Utils.isNullOrEmpty(edtRetypePassword.getText().toString()))
              {
                if (edtPassword.getText().toString().length() >= 4)
                {

                  if (edtPassword.getText().toString().equals(edtRetypePassword.getText().toString()))
                  {
                    btnEnable.setEnabled(false);

                    HttpClient.get(String.format(Constants.Server.OAuth.GET_CHANGE_PASSWORD, MyApplication.twoStepPass, edtPassword.getText().toString()), new AsyncHttpResponser(getContext()) {
                      @Override
                      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        super.onSuccess(statusCode, headers, responseBody);
                        final SimpleDialog dialogSuccess = new SimpleDialog(getContext());
                        dialogSuccess.message(R.string.message_success_changepass_twostep)
                          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                          .positiveAction(R.string.ok)
                          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                          .setCancelable(true);
                        dialogSuccess.positiveActionClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                            dialogSuccess.dismiss();
                            FragmentHandler.onBackPressed(getContext());
                          }
                        });
                        dialogSuccess.show();
                      }

                      @Override
                      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                        btnEnable.setEnabled(true);
                        if (statusCode == 400)
                        {
                          Toast.makeText(getContext(), R.string.toast_change_pass_twostep_failed, Toast.LENGTH_SHORT).show();
                        }
                      }
                    });
                  } else

                  {
                    edtRetypePassword.setError(getResources().getString(R.string.error_pass_notmatch));
                  }

                } else
                {
                  edtPassword.setError(getString(R.string.error_password_lenth));
                }
              } else
              {
                Toast.makeText(getContext(), R.string.error_fill, Toast.LENGTH_SHORT).show();
              }

            }
          });
          edtEmail.setVisibility(View.GONE);
          txtDesc.setText(getResources().getString(R.string.desc_set_password_twostep));

          break;
      }

      return view;
    } else
    {
      return view;
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId())
    {
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
