package com.collect_up.c_up.fragments;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.GetStartActivity;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.ILocationCallback;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Initial;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.services.CheckInternetConnectivity;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.CustomMultiPermissionDialog;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.orhanobut.hawk.Hawk;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.SimpleDialog;
import com.yalantis.ucrop.model.MediaContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 9/1/2016.
 */
public class FragmentRegister extends Fragment
  implements Validator.ValidationListener, ChoosePhoto.OnDialogButtonClick {

  private Validator mValidator;

  @NotEmpty (trim = true)
  private EditText mNameEditText;
  @Length (min = 5)
  @NotEmpty (trim = true)
  private EditText mUsernameEditText;

  private RectangleNetworkImageView mProfilePicture;
  private BottomSheetDialog mDialog;
  private Button mRegisterButton;
  private Intent realtimeServiceIntent;
  private Intent checkConnectivityServiceIntent;

  @Override
  public void onDialogTakePhoto() {
    mDialog.dismiss();
  }

  @Override
  public void onDialogRemovePhoto() {
    mDialog.dismiss();
    mProfilePicture.setImageResource(R.drawable.ic_camera_gray);
    mProfilePicture.setTag(null);
  }

  @Override
  public void onDialogFromGallery() {
    Utils.pickImage(this);
    mDialog.dismiss();

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null)
    {
      MediaContent content = (MediaContent) data.getBundleExtra("data").getParcelableArrayList("data").get(0);
      mProfilePicture.setImageURI(content.getUri());
      mProfilePicture.setTag("changed");
    }
  }

  @Override
  public void onDialogFromVideo() {

  }

  private void registerNewProfile(final Profile profile, final com.rey.material.app.SimpleDialog loadingDialog) {

    CustomMultiPermissionDialog dialogPermissionListener =
      CustomMultiPermissionDialog.Builder
        .withContext(getActivity())
        .withTitle(R.string.permission_title)
        .withMessage(R.string.permission_contact)
        .withButtonText(android.R.string.ok)
        .build();
    MultiplePermissionsListener basePermission = new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
        {
          profile.setPhoneNumber(GetStartFragments.phoneNumber);
          if (mProfilePicture.getTag() != null)
          {
            profile.setImageAddress((String) mProfilePicture.getTag());
          } else
          {
            profile.setImageAddress(null);
          }

          List<String> contacts = Utils.getContactsMobileNumbers(getActivity());
          final List<String> standardizedContacts = new ArrayList<>();

          for (String contact : contacts)
          {
            standardizedContacts.add(Utils.standardizePhoneNumber(contact, profile.getCountryCode()));
          }

          profile.setFollowing(standardizedContacts);

          Logged.Models.setUserProfile(profile);

          HttpClient.put(getActivity(), Constants.Server.Profile.PUT, new Gson().toJson(profile, Profile.class), "application/json", new AsyncHttpResponser(getContext(), getActivity().getMainLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              loadingDialog.dismiss();
              Profile profile = GsonParser.getObjectFromGson(responseBody, Profile.class);
              Hawk.put("profileId", profile.getId());
              Logged.Models.setUserProfile(profile);


              Intent intent = new Intent(getActivity(), ActivityHome.class);
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              emptyBackStack();

              getActivity().stopService(realtimeServiceIntent);
              getActivity().stopService(checkConnectivityServiceIntent);

              getActivity().startService(new Intent(getActivity(), RealtimeService.class));
              getActivity().startService(new Intent(getActivity(), CheckInternetConnectivity.class));

              Utils.syncContacts(getContext(), TextUtils.join(",", standardizedContacts));

              startActivity(intent);
              getActivity().finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

              super.onFailure(statusCode, headers, responseBody, error);
              getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  mRegisterButton.setEnabled(true);
                  loadingDialog.dismiss();
                  Toast.makeText(getActivity(), R.string.toast_error_connectivity_problem, Toast.LENGTH_LONG).show();
                }
              });

            }
          });
        }
      }

      @Override
      public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
      }
    };

    CompositeMultiplePermissionsListener compositePermissionListener = new CompositeMultiplePermissionsListener(basePermission, dialogPermissionListener);
    Dexter.withActivity(getActivity())
      .withPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION)
      .withListener(compositePermissionListener)
      .check();


  }


  @Override
  public void onValidationSucceeded() {

    CustomMultiPermissionDialog dialogPermissionListener =
      CustomMultiPermissionDialog.Builder
        .withContext(getActivity())
        .withTitle(R.string.permission_title)
        .withMessage(R.string.permission_contact)
        .withButtonText(android.R.string.ok)
        .build();
    MultiplePermissionsListener basePermission = new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
        {
          if (mRegisterButton.getText().toString().equals(getString(R.string.ok)))
          {
            final com.rey.material.app.SimpleDialog loadingDialog = Utils.createLoadingDialog(getActivity());
            loadingDialog.show();


            // OK Button
            mRegisterButton.setEnabled(true);

            if (mProfilePicture.getTag() != null && mProfilePicture.getTag().toString().equalsIgnoreCase("changed"))
            {
              try {
                new Upload(getContext(), new File(mProfilePicture.getImageURI().getPath()), UUID.randomUUID().toString(), "image/jpeg").uploadImage(new IUploadCallback() {
                  @Override
                  public void onFileReceived(String fileName, String uploadedPath) {
                    mProfilePicture.setTag(uploadedPath);
                    putProfile(loadingDialog);

                  }

                  @Override
                  public void onFailed(int statusCode) {
                    loadingDialog.dismiss();

                  }

                  @Override
                  public void onProgress(long bytesWritten, long totalSize) {

                  }
                });
              }
              catch (Exception ex){

              }
            } else
            {
              putProfile(loadingDialog);
            }


          } else
          {
            // Register Button
            final com.rey.material.app.SimpleDialog loadingDialog = Utils.createLoadingDialog(getActivity());
            loadingDialog.show();
            HttpClient.get(String.format(Constants.Server.OAuth.GET_VALIDATE_USERNAME, mUsernameEditText.getText().toString().trim()), new JsonHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try
                {
                  if (response.getBoolean("Reserved"))
                  {

                    getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        mRegisterButton.setEnabled(false);
                      }
                    });

                    final Profile profile = new Profile();
                    profile.setSettingsUsername(getResources().getBoolean(R.bool.prefShowUsername));
                    profile.setSettingsPhoneNumber(getResources().getBoolean(R.bool.prefShowPhoneNumber));
                    profile.setSettingsLocation(getResources().getBoolean(R.bool.prefShowLocation));
                    profile.setSettingsLanguages(getResources().getBoolean(R.bool.prefShowLanguages));
                    profile.setSettingsJob(getResources().getBoolean(R.bool.prefShowJob));
                    profile.setIsMan(true);
                    profile.setSettingsGender(getResources().getBoolean(R.bool.prefShowGender));
                    profile.setSettingsBiography(getResources().getBoolean(R.bool.prefShowBiography));
                    profile.setSettingsBirthday(getResources().getBoolean(R.bool.prefShowBirthday));
                    profile.setSettingsEmail(getResources().getBoolean(R.bool.prefShowEmail));
                    // Prevent BlockList from being null
                    profile.setBlockList("");
                    profile.setName(mNameEditText.getText().toString().trim());
                    profile.setUsername(mUsernameEditText.getText().toString().trim().toLowerCase());
                    profile.setCountryCode(Integer.parseInt(GetStartFragments.areaCode));
                    // Set last known location of the user in the profile object
                    getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        new Location(getContext()).getLocation(new ILocationCallback() {
                          @Override
                          public void onLocationFound(LatLng latLng) {
                            profile.setLong(Double.toString(latLng.longitude));
                            profile.setLat(Double.toString(latLng.latitude));

                            registerNewProfile(profile, loadingDialog);
                          }

                          @Override
                          public void onCouldNotGetLocation() {
                            profile.setLat("0");
                            profile.setLong("0");
                            registerNewProfile(profile, loadingDialog);
                          }
                        });
                      }
                    });
                  } else
                  {
                    getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        loadingDialog.dismiss();
                        mUsernameEditText.setError(getString(R.string.invalid_username));
                        mRegisterButton.setEnabled(false);
                      }
                    });
                  }
                } catch (JSONException e)

                {
                  e.printStackTrace();
                }
              }

              @Override
              public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, Throwable
                throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                loadingDialog.dismiss();
                Toast.makeText(getActivity(), "error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray
                errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                loadingDialog.dismiss();
                Toast.makeText(getActivity(), "error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, String
                responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loadingDialog.dismiss();
                Toast.makeText(getActivity(), "error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
              }
            });
          }
        }
      }

      @Override
      public void onPermissionRationaleShouldBeShown
        (List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
      }
    };
    CompositeMultiplePermissionsListener compositePermissionListener = new CompositeMultiplePermissionsListener(basePermission, dialogPermissionListener);
    Dexter.withActivity(getActivity())
      .withPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION)
      .withListener(compositePermissionListener)
      .check();


  }

  private void putProfile(final SimpleDialog loadingDialog) {
    final Profile profile = Logged.Models.getUserProfile();

    profile.setImageAddress((String) mProfilePicture.getTag());
    profile.setName(mNameEditText.getText().toString().trim());
    profile.setCountryCode(Integer.valueOf(GetStartFragments.areaCode));
    profile.setPhoneNumber(GetStartFragments.phoneNumber);

    Hawk.put("profile", profile);
    HttpClient.put(getActivity(), Constants.Server.Profile.PUT, new Gson().toJson(Logged.Models.getUserProfile(), Profile.class), "application/json", new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        loadingDialog.dismiss();
        mRegisterButton.setEnabled(false);

        Intent intent = new Intent(getActivity(), ActivityHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //emptyBackStack();

        getActivity().stopService(realtimeServiceIntent);
        getActivity().stopService(checkConnectivityServiceIntent);

        getActivity().startService(new Intent(getActivity(), RealtimeService.class));
        getActivity().startService(new Intent(getActivity(), CheckInternetConnectivity.class));

        List<String> contacts = Utils.getContactsMobileNumbers(getActivity());
        final List<String> standardizedContacts = new ArrayList<>();

        for (String contact : contacts)
        {
          standardizedContacts.add(Utils.standardizePhoneNumber(contact, Logged.Models.getUserProfile().getCountryCode()));
        }

        Utils.syncContacts(getContext(), TextUtils.join(",", standardizedContacts));

        startActivity(intent);
        getActivity().finish();
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);
        loadingDialog.dismiss();
        Toast.makeText(getActivity(), R.string.toast_error_connectivity_problem, Toast.LENGTH_LONG).show();
      }
    });

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

  /**
   * Empties the back stack
   */
  private void emptyBackStack() {
    FragmentManager fm = getActivity().getSupportFragmentManager();
    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    realtimeServiceIntent = new Intent(getActivity(), RealtimeService.class);
    checkConnectivityServiceIntent = new Intent(getActivity(), CheckInternetConnectivity.class);

    // There is no API to check whether a receiver is registered or not.
    try
    {
      getActivity().unregisterReceiver(GetStartActivity.mSmsReceiver);
      getActivity().unregisterReceiver(GetStartActivity.mCallReceiver);
    } catch (IllegalArgumentException ignored)
    {

    }

    getActivity().startService(realtimeServiceIntent);
    getActivity().startService(checkConnectivityServiceIntent);

    final View inflate = inflater.inflate(R.layout.fragment_get_start_register, container, false);

    mValidator = new Validator(this);
    mValidator.setValidationListener(this);

    // Initialize views
    mNameEditText = (EditText) inflate.findViewById(R.id.edit_text_name);
    mUsernameEditText = (EditText) inflate.findViewById(R.id.edit_text_username);
    mRegisterButton = (Button) inflate.findViewById(R.id.button_register);

    mProfilePicture = (RectangleNetworkImageView) inflate.findViewById(R.id.image_view_picture);
    mProfilePicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog = new ChoosePhoto(getActivity(), FragmentRegister.this, false).show();
      }
    });

    mRegisterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mValidator.validate(true);
      }
    });
    mUsernameEditText.addTextChangedListener(new TextWatcher() {
                                               @Override
                                               public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                               }

                                               @Override
                                               public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                 if (mRegisterButton.getText().toString().equalsIgnoreCase(getString(R.string.register)))
                                                 {
                                                   HttpClient.get(String.format(Constants.Server.OAuth.GET_VALIDATE_USERNAME, mUsernameEditText.getText().toString().trim()), new JsonHttpResponser(getContext()) {
                                                     @Override
                                                     public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                       super.onSuccess(statusCode, headers, response);
                                                       try
                                                       {
                                                         if (response.getBoolean("Reserved"))
                                                         {

                                                           getActivity().runOnUiThread(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                               mRegisterButton.setEnabled(true);
                                                               mUsernameEditText.setError(null);
                                                             }
                                                           });
                                                         } else
                                                         {
                                                           getActivity().runOnUiThread(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                               mUsernameEditText.setError(getString(R.string.invalid_username));
                                                               mRegisterButton.setEnabled(false);
                                                             }
                                                           });
                                                         }
                                                       } catch (JSONException e)

                                                       {
                                                         e.printStackTrace();
                                                       }
                                                     }

                                                     @Override
                                                     public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                                       super.onSuccess(statusCode, headers, response);
                                                     }

                                                     @Override
                                                     public void onFailure(int statusCode, Header[] headers, Throwable
                                                       throwable, JSONObject errorResponse) {
                                                       super.onFailure(statusCode, headers, throwable, errorResponse);
                                                     }

                                                     @Override
                                                     public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray
                                                       errorResponse) {
                                                       super.onFailure(statusCode, headers, throwable, errorResponse);
                                                     }

                                                     @Override
                                                     public void onFailure(int statusCode, Header[] headers, String
                                                       responseString, Throwable throwable) {
                                                       super.onFailure(statusCode, headers, responseString, throwable);
                                                     }
                                                   });
                                                 }
                                               }

                                               @Override
                                               public void afterTextChanged(Editable s) {

                                               }
                                             }

    );
    final com.rey.material.app.SimpleDialog dialog = Utils.createLoadingDialog(getActivity());
    dialog.show();

    HttpClient.get(String.format(Constants.Server.Profile.GET_INITIAL_DATA), new

      AsyncHttpResponser(getContext()

      )

      {
        @Override
        public void onSuccess(int statusCode,
                              Header[] headers,
                              byte[] responseBody) {

          Initial initial = GsonParser.getObjectFromGson(responseBody, Initial.class);
          Profile profile = initial.getProfile();
          Logged.Models.setUserProfile(profile);
          Hawk.put("profileId", profile.getId());

          Shop shop = initial.getShop();
          Logged.Models.setUserShop(shop);

          Complex complex = initial.getComplex();
          Logged.Models.setUserComplex(complex);

          List<Shop> shopList = initial.getShopList();
          Logged.Models.setUserShopList(shopList);

          List<Complex> complexList = initial.getComplexList();
          Logged.Models.setUserComplexList(complexList);

          assert profile != null;
          if (!Utils.isNullOrEmpty(profile.getImageAddress()))
          {
            mProfilePicture.setImageUrl(Constants.General.BLOB_PROTOCOL + profile.getThumb(), MyApplication.getInstance().getImageLoader());
            mProfilePicture.setTag(profile.getThumb());
          }

          mNameEditText.setText(profile.getName());
          if (profile.getUsername() != null)
          {
            mRegisterButton.setText(R.string.ok);
            mRegisterButton.setTag("login");
            mRegisterButton.setEnabled(true);
            mUsernameEditText.setEnabled(false);
            mUsernameEditText.setText(profile.getUsername());
          } else
          {
            mUsernameEditText.setError(null);
            mRegisterButton.setEnabled(true);
            mRegisterButton.setText(R.string.register);
            mRegisterButton.setTag("register");
          }

          dialog.dismiss();

        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          if (statusCode == 404)
          {
            dialog.dismiss();
            mRegisterButton.setTag("register");


          } else
          {
            dialog.dismiss();
            mRegisterButton.setEnabled(false);
            Toast.makeText(getActivity(), R.string.toast_error_connectivity_problem, Toast.LENGTH_LONG).show();
          }
        }
      }

    );

    return inflate;
  }


}