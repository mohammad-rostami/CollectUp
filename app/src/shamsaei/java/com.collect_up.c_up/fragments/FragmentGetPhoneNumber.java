package com.collect_up.c_up.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityGalleryPager;
import com.collect_up.c_up.activities.GetStartActivity;
import com.collect_up.c_up.adapters.CountryAutoCompleteAdapter;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Files;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.view.CustomMultiPermissionDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/1/2016.
 */
public class FragmentGetPhoneNumber extends Fragment
  implements Validator.ValidationListener {

  private Validator mValidator;

  @NotEmpty
  private EditText mAreaCodeEditText;
  private Spinner mAutoCompleteTextView;
  @NotEmpty
  @Length (trim = true)
  private EditText mPhoneNumberEditText;

  @Override
  public void onValidationSucceeded() {

    if (Hawk.get("smsLimit") == null)
    {
      Hawk.put("smsLimit", 0);
      checkSmsLimit();
    } else
    {
      if ((int) Hawk.get("smsLimit") > 10)
      {

        if (Hawk.get("timer") != null)
        {

          long timerInHawk = Hawk.get("timer");
          // String timeAgo = TimeHelper.getTimeAgo(getActivity(), timerInHawk);
          long currntTime = System.currentTimeMillis();
          long dif = currntTime - timerInHawk;
          if (timerInHawk != 0 && dif > TimeHelper.HOUR_MILLIS * 3)
          {
            Toast.makeText(getActivity(), R.string.toast_error_sms_limitation_exceed, Toast.LENGTH_LONG).show();
          } else
          {
            Hawk.put("smsLimit", 0);
            checkSmsLimit();
          }

        } else
        {
          checkSmsLimit();
        }


      } else
      {
        checkSmsLimit();
      }
    }


  }

  private void checkSmsLimit() {
    // After validation succeeded, replace this fragment
    // by activate fragment.
    MultiplePermissionsListener dialogMultiplePermissionsListener =
      CustomMultiPermissionDialog.Builder
        .withContext(getActivity())
        .withTitle(R.string.permission_title)
        .withMessage(R.string.permission_calls_sms)
        .withButtonText(android.R.string.ok)
        .build();
    MultiplePermissionsListener multiPermission = new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
        {
          GetStartFragments.phoneNumber = mAreaCodeEditText.getText().toString() + mPhoneNumberEditText.getText().toString();
          GetStartFragments.areaCode = mAreaCodeEditText.getText().toString();
          HttpClient.get(String.format(Constants.Server.OAuth.GET_SECURITY_STATE, GetStartFragments.phoneNumber), new JsonHttpResponser(getContext()) {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
              super.onFailure(statusCode, headers, responseString, throwable);
              GetStartFragments.hasTwoStep = false;
              GetStartFragments.isEmailVerified = false;
              ((GetStartActivity) getActivity()).replaceFragment(new FragmentActivate());

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
              super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
              super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
              super.onSuccess(statusCode, headers, response);
              try
              {
                GetStartFragments.hasTwoStep = response.getBoolean("TwoStepVerificationEnabled");
                GetStartFragments.isEmailVerified = response.getBoolean("EmailVerified");
                ((GetStartActivity) getActivity()).replaceFragment(new FragmentActivate());

              } catch (JSONException e)
              {
                e.printStackTrace();
              }

            }
          });

        }
      }

      @Override
      public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
      }
    };
    CompositeMultiplePermissionsListener compositePermissionListener = new CompositeMultiplePermissionsListener(multiPermission, dialogMultiplePermissionsListener);
    Dexter.withActivity(getActivity())
      .withPermissions(Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE)
      .withListener(compositePermissionListener)
      .check();

  }

  @Override
  public void onValidationFailed(List<ValidationError> errors) {
    for (ValidationError error : errors)
    {
      View view = error.getView();
      String message = error.getCollatedErrorMessage(getActivity());
      if (view instanceof EditText)
      {
        ((EditText) view).setError(message);
      }
    }
  }

  private String getCountryCode(String countryName) {
    for (String datum : getCountries())
    {
      String[] data = datum.split(",");
      if (data[0].toLowerCase().startsWith(countryName.toLowerCase().trim()))
      {
        return data[1];
      }
    }

    return null;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    final View inflate = inflater.inflate(R.layout.fragment_get_phone_number, container, false);

    mValidator = new Validator(this);
    mValidator.setValidationListener(this);

    // Initialize views
    mAreaCodeEditText = (EditText) inflate.findViewById(R.id.edit_text_area_code);
    mPhoneNumberEditText = (EditText) inflate.findViewById(R.id.edit_text_phone_number);
    mAutoCompleteTextView = (Spinner) inflate.findViewById(R.id.auto_complete_text_view_countries);

    mPhoneNumberEditText.setFilters(new InputFilter[] {Utils.getPhoneNumberInputFilter()});

    List<String> dataCountries = getCountries();

    String defaultCountryCode = Utils.getSimCardCountryCode(getContext(), dataCountries);
    if (!Utils.isNullOrEmpty(defaultCountryCode))
    {
      String[] dataCountry = defaultCountryCode.split(",");
      mAreaCodeEditText.setText(dataCountry[1]);

      final int indexOfCountry = dataCountries.indexOf(defaultCountryCode);

      mAutoCompleteTextView.setSelection(indexOfCountry);
      mAutoCompleteTextView.post(new Runnable() {
        @Override
        public void run() {
          mAutoCompleteTextView.setSelection(indexOfCountry);
        }
      });
    }


    // Following lines are related to country auto complete text view.
    mAutoCompleteTextView.setAdapter(new CountryAutoCompleteAdapter(getActivity(), dataCountries));
    mAutoCompleteTextView.post(new Runnable() {
      @Override
      public void run() {
        mAutoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            TextView textView = (TextView) view;
            mAreaCodeEditText.setText(getCountryCode(textView.getText().toString()));
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            mAreaCodeEditText.setText("");
          }
        });
      }
    });

    // Following line is related to register button.
    inflate.findViewById(R.id.button_register)
      .setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          //   if (SepehrUtil.validatePhonenumber(mAreaCodeEditText.getText().toString() + mPhoneNumberEditText.getText().toString())) {

          mValidator.validate(true);
          // } else {
          //    mPhoneNumberEditText.setError(getResources().getString(R.string.invalid_phone_number));
          //}///mValidator.validate(false);
        }
      });
    return inflate;
  }


  /**
   * Get Countries which are read from file as List
   *
   * @return Countries as List
   */
  private List<String> getCountries() {
    List<String> countries = new ArrayList<>();
    String fileContent = null;
    try
    {
      fileContent = Files.readFileContentFromAssets(getActivity(), "countries.txt");
    } catch (IOException e)
    {
    }
    assert fileContent != null;
    String[] countriesWithCode = fileContent.split("/");

    countries.addAll(Arrays.asList(countriesWithCode));
    return countries;
  }
}
