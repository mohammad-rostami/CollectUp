package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.GetStartActivity;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.LoginCredential;
import com.collect_up.c_up.model.VerificationResult;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.rey.material.app.SimpleDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/1/2016.
 */
@SuppressLint ("ValidFragment")
public class FragmentTwoStep extends Fragment {
  private final LoginCredential _LoginObject;

  public FragmentTwoStep(LoginCredential loginObject) {
    this._LoginObject = loginObject;
  }

  @Bind (R.id.edtPassword)
  EditText edtPassword;
  @Bind (R.id.btnLogin)
  Button btnLogin;
  @Bind (R.id.btnForgot)
  TextView btnForgot;

  @Nullable
  @Override


  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_twostep, container, false);
    ButterKnife.bind(this, view);
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
            HttpClient.get(String.format(Constants.Server.OAuth.GET_RESET_PASSWORD, GetStartFragments.phoneNumber), new AsyncHttpResponser(getContext()) {
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
    btnLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!Utils.isNullOrEmpty(edtPassword.getText().toString()))
        {
          if (edtPassword.getText().toString().length() >= 4)
          {
            _LoginObject.setPassword((edtPassword.getText().toString()));
            btnLogin.setEnabled(false);

            HttpClient.post(getContext(), Constants.Server.OAuth.LOGIN_CREDENTIAL, new Gson().toJson(_LoginObject), "application/json", new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                VerificationResult result = GsonParser.getObjectFromGson(responseBody, VerificationResult.class);
                Hawk.put("Token", result.getToken());


                ((GetStartActivity) getActivity()).replaceFragment(new FragmentRegister());
                GetStartFragments.isVerified = true;

              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                Toast.makeText(getContext(), R.string.toast_error_enter_twostep, Toast.LENGTH_SHORT).show();
                GetStartFragments.isVerified = false;
                btnLogin.setEnabled(true);


              }
            });
          } else
          {
            edtPassword.setError(getString(R.string.error_password_lenth));
          }
        } else
        {
          edtPassword.setError(getString(R.string.error_fill));
        }
      }
    });

    return view;
  }
}
