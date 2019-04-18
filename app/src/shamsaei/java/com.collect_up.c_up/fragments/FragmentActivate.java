package com.collect_up.c_up.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.GetStartActivity;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.LoginCredential;
import com.collect_up.c_up.model.Register;
import com.collect_up.c_up.model.VerificationResult;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/1/2016.
 */
public class FragmentActivate extends Fragment {
  private TextView txtTimer;
  private EditText mActivationCodeEditText;
  private TextView txtLblActivation;
  private CountDownTimer countDownTimer;
  private int counter = 0;
  private boolean isCalled = false;

  private String getReadableTime(int val) {
    int minutes = val / 60;
    int baghi = val % 60;
    String output;
    if (minutes > 0) {
      output = "0" + minutes + ":";
    } else {
      output = "00:";
    }
    if (baghi < 10) {
      output += "0" + baghi;
    } else {
      output += baghi;
    }

    return output;
  }

  private void countDown() {
    countDownTimer = new CountDownTimer(TimeHelper.MINUTE_MILLIS * 2, 1000) {
      public void onTick(long millisUntilFinished) {
        counter++;
        Log.i("sepehr", "counter: " + counter + " tick: " + millisUntilFinished);

        if (txtTimer != null && isVisible()) {
          txtTimer.setText(getReadableTime((int) (millisUntilFinished / 1000)));
          if (counter == Constants.General.CALL_AFTER && !isCalled) {
            HttpClient.post(getContext(), String.format(Constants.Server.OAuth.MAKE_CALL, GetStartFragments.phoneNumber), "application/json", new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                isCalled = true;
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                Log.i("sepehr", "onFailure: call failed");
              }
            });
          }
        }
      }

      public void onFinish() {
        if (txtTimer != null && isVisible()) {
          txtTimer.setText(getString(R.string.try_again));
        }
      }
    }.start();


  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    final View inflate = inflater.inflate(R.layout.fragment_get_start_activate, container, false);

    GetStartFragments.tryAgainPressed = false;

    mActivationCodeEditText = (EditText) inflate.findViewById(R.id.edit_text_activation_code);
    txtTimer = (TextView) inflate.findViewById(R.id.text_view_timer);
    txtLblActivation = (TextView) inflate.findViewById(R.id.text_view_label_activation);
    txtLblActivation.setText(String.format(getContext().getString(R.string.activation_sent), GetStartFragments.phoneNumber));
    Register register = new Register();
    register.setPhoneNumber(GetStartFragments.phoneNumber);
    HttpClient.post(getContext(), Constants.Server.OAuth.REGISTER, new Gson().toJson(register), "application/json", new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        if (Hawk.get("smsLimit") == null) {
          Hawk.put("smsLimit", 0);
        }
        Hawk.put("smsLimit", (int) Hawk.get("smsLimit") + 1);
        if ((int) Hawk.get("smsLimit") > 10) {
          Hawk.put("timer", System.currentTimeMillis());
        }
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

        onErrorHandler(statusCode, getActivity());

      }
    });


    mActivationCodeEditText.setFilters(new InputFilter[]{Utils.getActivationCodeInputFilter()});
    countDown();

    txtTimer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (txtTimer.getText().toString().equals(getString(R.string.try_again))) {
          if ((int) Hawk.get("smsLimit") > 5) {

            if (Hawk.get("timer") != null) {

              long timerInHawk = Hawk.get("timer");
              String timeAgo = TimeHelper.getTimeAgo(getActivity(), timerInHawk);

              if (!Utils.isNullOrEmpty(timeAgo) && !timeAgo.contains("d")) {
                Toast.makeText(getActivity(), R.string.toast_error_sms_limitation_exceed, Toast.LENGTH_SHORT).show();
              } else {
                Hawk.put("smsLimit", 0);
                GetStartFragments.tryAgainPressed = true;
                getActivity().onBackPressed();
              }

            } else {
              GetStartFragments.tryAgainPressed = true;
              getActivity().onBackPressed();
            }


          } else {
            GetStartFragments.tryAgainPressed = true;
            getActivity().onBackPressed();
          }
        }
      }
    });

    inflate.findViewById(R.id.button_check).setOnClickListener(new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                   String activationCodeEditTextInput = mActivationCodeEditText.getText()
                                                                     .toString();
                                                                   if (!activationCodeEditTextInput.isEmpty()) {


                                                                     Hawk.put("smsLimit", 0);
                                                                     final LoginCredential login = new LoginCredential();
                                                                     login.setPhoneNumber(GetStartFragments.phoneNumber);
                                                                     login.setPassword("");
                                                                     // login.setIpAddress(SepehrUtil.getIpAddress(getContext()));
                                                                     login.setVerificationCode("shamsae");
                                                                     login.setDevice(SepehrUtil.getPhoneName() + "," + " Android SDK " + Build.VERSION.SDK_INT);
                                                                     login.setPlatform("Collect-up Android " + SepehrUtil.getVersionName(getContext()));


                                                                     if (GetStartFragments.hasTwoStep) {

                                                                       HttpClient.get(String.format(Constants.Server.OAuth.GET_VERIFY_CODE, GetStartFragments.phoneNumber, "shamsae"), new AsyncHttpResponser(getContext()) {
                                                                         @Override
                                                                         public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                           super.onSuccess(statusCode, headers, responseBody);
                                                                           if (countDownTimer != null) {
                                                                             countDownTimer.cancel();
                                                                           }
                                                                           GetStartFragments.isVerified = true;

                                                                           ((GetStartActivity) getActivity()).replaceFragment(new FragmentTwoStep(login));

                                                                         }

                                                                         @Override
                                                                         public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                                           super.onFailure(statusCode, headers, responseBody, error);

                                                                           GetStartFragments.isVerified = false;

                                                                           ((EditText) inflate.findViewById(R.id.edit_text_activation_code)).setError(getActivity()
                                                                             .getString(R.string.invalid_activation_code));
                                                                         }
                                                                       });

                                                                     } else {


                                                                       // if (!BuildConfig.DEBUG) {
                                                                       HttpClient.post(getContext(), Constants.Server.OAuth.LOGIN_CREDENTIAL, new Gson().toJson(login), "application/json", new AsyncHttpResponser(getContext()) {
                                                                         @Override
                                                                         public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                           if (countDownTimer != null) {
                                                                             countDownTimer.cancel();
                                                                           }
                                                                           VerificationResult result = GsonParser.getObjectFromGson(responseBody, VerificationResult.class);
                                                                           Hawk.put("Token", result.getToken());


                                                                           ((GetStartActivity) getActivity()).replaceFragment(new FragmentRegister());
                                                                           GetStartFragments.isVerified = true;

                                                                         }

                                                                         @Override
                                                                         public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                                           super.onFailure(statusCode, headers, responseBody, error);
                                                                           GetStartFragments.isVerified = false;

                                                                           ((EditText) inflate.findViewById(R.id.edit_text_activation_code)).setError(getActivity()
                                                                             .getString(R.string.invalid_activation_code));
                                                                         }
                                                                       });


                                                                     }
                                                                   } else {
                                                                     // If randomized code is not equals to read code from the message
                                                                     ((EditText) inflate.findViewById(R.id.edit_text_activation_code)).setError(getActivity()
                                                                       .getString(R.string.invalid_activation_code));
                                                                   }
                                                                 }
                                                               }

    );

    return inflate;
  }
}