/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ImageButton;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import me.gujun.android.taggroup.TagGroup;

@SuppressLint ("ValidFragment")

public class FragmentComplexNew extends BaseFragment implements GoogleMap.OnMapClickListener,
  Validator.ValidationListener,
  ChoosePhoto.OnDialogButtonClick, TagGroup.OnTagLimitationExceedListener {

  public static boolean isRunning;
  private final String mCurrentUserId = Logged.Models.getUserProfile().getId();
  private EditText mDescriptionEditText;
  private BottomSheetDialog mDialog;
  private EditText mEmailEditText;
  @NotEmpty (trim = true)
  private EditText mNameEditText;
  private EditText mPhoneNumberEditText;
  private LatLng mPickedLocation;
  private EditText mPresentationCodeEditText;
  private EditText mAddress;
  private RectangleNetworkImageView mProfilePictureImageView;
  private TagGroup mTagGroup;
  private ImageView imgCover;
  private ImageButton btnCoverEdit;
  @NotEmpty (trim = true)
  @Length (min = 5)
  private EditText mUsernameEditText;
  private Validator mValidator;
  private GoogleMap mGoogleMap;
  private Complex mComplex = new Complex();
  private Menu mMenu;
  private Button btnUpdate;
  private boolean isProfileSet;
  public boolean forcePost;
  private Intent profileImageIntent;
  private Intent coverImageIntent;
  private static final int MODE_COVER = 0;
  private static final int MODE_PROFILE = 1;
  private int mode;
  private SimpleDialog waitDialog;
  private SimpleDialog builder;
  private View view;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.PICK_LOCATION.ordinal() && resultCode == getActivity().RESULT_OK)
    {
      LatLng pickedLocation = data.getParcelableExtra("location");
      mPickedLocation = pickedLocation;
      mGoogleMap.clear();
      if (pickedLocation.latitude == 0 && pickedLocation.longitude == 0)
      {
      } else
      {
        mGoogleMap.addMarker(new MarkerOptions().position(pickedLocation));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickedLocation.latitude, pickedLocation.longitude), 5));
      }
    } else if (data != null)
    {
      handleResult(resultCode, data);
    }

  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  private void handleResult(int resultCode, Intent result) {
    if (resultCode == getActivity().RESULT_OK)
    {
      MediaContent content = (MediaContent) result.getBundleExtra("data").getParcelableArrayList("data").get(0);

      switch (mode)
      {
        case MODE_COVER:
          replaceCoverPicture(content.getUri());
          break;
        case MODE_PROFILE:
          replaceProfilePicture(content.getUri());
          break;
      }
    } else if (resultCode == UCrop.RESULT_ERROR)
    {
      Toast.makeText(getContext(), UCrop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void replaceCoverPicture(Uri uri) {
    try
    {
      imgCover.setTag(Images.getBitmapFromUri(getContext(), uri));
    } catch (IOException e)
    {
    }
    imgCover.setImageURI(null);

    imgCover.setImageURI(uri);
  }

  private void replaceProfilePicture(Uri uri) {
    try
    {
      mProfilePictureImageView.setTag(Images.getBitmapFromUri(getContext(), uri));
    } catch (IOException e)
    {
    }
    mProfilePictureImageView.setImageURI(uri);
    isProfileSet = true;

  }

  int counter = 0;
  int notNulls = 0;

  private void check() {
    if (counter == notNulls)
    {

      HttpClient.post(getContext(), String.format(Constants.Server.Complex.POST, Utils.isNullOrEmpty(mPresentationCodeEditText
        .getText()
        .toString()) ? 0 : mPresentationCodeEditText
        .getText()
        .toString().trim()), new Gson().toJson(mComplex, Complex.class), "application/json", new AsyncHttpResponser(getContext(), getActivity().getMainLooper()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              waitDialog.dismiss();
            }
          });

          Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());
          String id = Utils.removeExtraQuotations(new String(responseBody));
          mComplex.setId(id);
          Logged.Models.setUserComplex(mComplex);
          FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, mComplex);
          waitDialog.dismiss();

        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mMenu.findItem(R.id.action_done).setVisible(true);
              mMenu.findItem(R.id.menu_loader).setVisible(false);

              waitDialog.dismiss();

              Toast.makeText(getContext(), getString(R.string.toast_new_complex_failed), Toast.LENGTH_SHORT)
                .show();
            }
          });
        }
      });
    }
  }

  private void uploadPicture(String path, final int mode) {
    final boolean doneAlreadyEnabled = mMenu.findItem(R.id.action_done).isEnabled();

    if (mMenu != null)
    {
      mMenu.findItem(R.id.action_done).setVisible(false);
      mMenu.findItem(R.id.menu_loader).setVisible(false);

    }
    waitDialog = Utils.createLoadingDialog(getContext());
    waitDialog.show();
    if (builder != null)
    {
      builder.dismiss();
    }


    String filePath = path;//Images.getRealPathFromURI(this, Images.getImageUriFromBitmap(this, bitmap));
    new Upload(getContext(), new File(filePath), UUID.randomUUID().toString(), "image/jpeg")
      .uploadImage(new IUploadCallback() {
        @Override
        public void onFileReceived(String fileName, String uploadedPath) {
          waitDialog.dismiss();
          if (mode == MODE_PROFILE)
          {
            mComplex.setImageAddress(uploadedPath);
            counter++;
          } else
          {
            mComplex.setCoverPhoto(uploadedPath);
            counter++;
          }


          if (doneAlreadyEnabled)
          {
            if (mMenu != null)
            {
              mMenu.findItem(R.id.action_done).setVisible(true);
              mMenu.findItem(R.id.menu_loader).setVisible(false);

            }
          }
          check();


        }

        @Override
        public void onFailed(int statusCode) {
          waitDialog.dismiss();
          counter++;
          check();
          if (doneAlreadyEnabled)
          {
            if (mMenu != null)
            {
              mMenu.findItem(R.id.action_done).setVisible(true);
              mMenu.findItem(R.id.menu_loader).setVisible(false);

            }
          }
          if (mode == MODE_PROFILE)
          {
            mProfilePictureImageView.setImageResource(R.drawable.ic_camera_gray);
          } else
          {
            imgCover.setImageResource(R.drawable.blur_background);
          }


          Toast.makeText(getContext(), getString(R.string.toast_error_upload_image_failed), Toast.LENGTH_SHORT)
            .show();
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {

        }
      });
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.new_complex);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_complex_new, container, false);
      setHasOptionsMenu(true);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
      builder.message(R.string.complex_desc)
        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
        .title(R.string.dialog_complex)
        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .positiveAction(R.string.ok)
        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .setCancelable(true);

      builder.positiveActionClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          builder.dismiss();

        }
      });
      builder.show();


      // Set title to the text view
      ((TextView) view.findViewById(R.id.text_view_title)).setText(R.string.new_complex);

      mNameEditText = (EditText) view.findViewById(R.id.edit_text_name);
      mEmailEditText = (EditText) view.findViewById(R.id.edit_text_email);
      mPhoneNumberEditText = (EditText) view.findViewById(R.id.edit_text_phone_number);
      mAddress = (EditText) view.findViewById(R.id.edit_text_address);
      mUsernameEditText = (EditText) view.findViewById(R.id.edit_text_username);
      mPresentationCodeEditText = (EditText) view.findViewById(R.id.edit_text_presentation_code);
      mDescriptionEditText = (EditText) view.findViewById(R.id.edit_text_description);
      mProfilePictureImageView = (RectangleNetworkImageView) view.findViewById(R.id.image_view_picture);
      imgCover = (ImageView) view.findViewById(R.id.image_view_background);
      btnCoverEdit = (ImageButton) view.findViewById(R.id.btnEditCover);

      mTagGroup = (TagGroup) view.findViewById(R.id.tag_group);
      btnUpdate = (Button) view.findViewById(R.id.btnUpdate);
      btnUpdate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), ActivityPickLocation.class);
          intent.putExtra("return", true);
          intent.putExtra("location", mPickedLocation);
          startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
        }
      });
      mTagGroup.setLimitation(Constants.General.TAG_LIMITATION);
      mTagGroup.setOnLimitationExceedListener(this);


      mUsernameEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

          HttpClient.get(String.format(Constants.Server.OAuth.GET_VALIDATE_USERNAME, mUsernameEditText.getText().toString()), new JsonHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
              super.onSuccess(statusCode, headers, response);

              try
              {
                if (response.getBoolean("Reserved"))
                {
                  mUsernameEditText.setError(null);
                  if (mMenu != null)
                  {
                    mMenu.findItem(R.id.action_done).setVisible(true);
                    mMenu.findItem(R.id.menu_loader).setVisible(false);

                  }
                } else
                {
                  mUsernameEditText.setError(getString(R.string.invalid_username));
                  if (mMenu != null)
                  {
                    mMenu.findItem(R.id.action_done).setVisible(false);
                    mMenu.findItem(R.id.menu_loader).setVisible(true);

                  }
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
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
              super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
              super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
              super.onFailure(statusCode, headers, responseString, throwable);
            }
          });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
      });

      // Request focus on the name field
      mNameEditText.requestFocus();

      mProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mDialog = new ChoosePhoto(getActivity(), FragmentComplexNew.this, false).show();
          mode = MODE_PROFILE;

        }
      });
      btnCoverEdit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mDialog = new ChoosePhoto(getActivity(), FragmentComplexNew.this, false).show();
          mode = MODE_COVER;

        }
      });

      ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view)).getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
          mGoogleMap = googleMap;
          mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
          mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
          mGoogleMap.getUiSettings().setCompassEnabled(false);
          mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
          mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
          mGoogleMap.setOnMapClickListener(FragmentComplexNew.this);

          LatLng latLng = new LatLng(Double.valueOf(Logged.Models.getUserProfile().getLat()), Double.valueOf(Logged.Models.getUserProfile().getLong()));
          if (latLng.latitude == 0 && latLng.longitude == 0)
          {
          } else
          {
            mGoogleMap.addMarker(new MarkerOptions().position(latLng)
              .title(getString(R.string.placed_here)));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 5));
          }
          mPickedLocation = latLng;
        }
      });

      mValidator = new Validator(this);
      mValidator.setValidationListener(this);

      CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
      collapsingToolbarLayout.setTitleEnabled(false);
      collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
      collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));
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
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_done_discard, menu);
    MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
    Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
    if (menuItemLoaderIcon != null)
    {
      try
      {
        menuItemLoaderIcon.mutate();
        menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menuItemLoader.setIcon(menuItemLoaderIcon);
      } catch (IllegalStateException e)
      {
        Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
      }
      ((Animatable) menuItemLoader.getIcon()).start();
      menuItemLoader.setVisible(false);
    }
    menu.findItem(R.id.action_search).setVisible(false);
    mMenu = menu;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
      case R.id.action_done:
        if (!isProfileSet)
        {
          Toast.makeText(getContext(), getString(R.string.validate_image), Toast.LENGTH_SHORT).show();
        } else if (getTags().size() < 2)
        {
          Toast.makeText(getContext(), getString(R.string.validate_tags), Toast.LENGTH_SHORT).show();

        } else
        {
          mValidator.validate(true);
        }

        break;
    }
    return false;
  }

  @Override
  public void onDialogTakePhoto() {
    mDialog.dismiss();
  }

  @Override
  public void onDialogRemovePhoto() {
    mDialog.dismiss();
    switch (mode)
    {
      case MODE_COVER:
        imgCover.setImageResource(R.drawable.blur_background);
        imgCover.setTag(null);
        mComplex.setCoverPhoto(null);


        break;
      case MODE_PROFILE:
        mProfilePictureImageView.setImageResource(R.drawable.ic_camera_gray);
        mProfilePictureImageView.setTag(null);
        mComplex.setImageAddress(null);
        isProfileSet = false;

        break;
    }


  }

  @Override
  public void onDialogFromGallery() {
    Utils.pickImage(this);

    mDialog.dismiss();
  }

  @Override
  public void onDialogFromVideo() {

  }

  @Override
  public void onMapClick(final LatLng latLng) {
  }

  @Override
  public void onValidationSucceeded() {
    mMenu.findItem(R.id.action_done).setVisible(false);
    mMenu.findItem(R.id.menu_loader).setVisible(true);

    if (mPickedLocation != null && mPickedLocation.latitude == 0 && mPickedLocation.longitude == 0 && !forcePost)
    {
      builder = new com.rey.material.app.SimpleDialog(getContext());
      builder.message(R.string.alert_message_pick_location)
        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
        .title(R.string.alert_title_pick_location)
        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .positiveAction(R.string.alert_positive_pick_location)
        .negativeAction(R.string.alert_negetive_pick_location)
        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .setCancelable(false);

      builder.negativeActionClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mMenu.findItem(R.id.action_done).setVisible(true);
          mMenu.findItem(R.id.menu_loader).setVisible(false);


          builder.dismiss();
          forcePost = false;
        }
      });
      builder.positiveActionClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

          mValidator.validate(true);
          forcePost = true;
        }
      });

      builder.show();
    } else
    {
      HttpClient.get(String.format(Constants.Server.OAuth.GET_VALIDATE_USERNAME, mUsernameEditText.getText().toString()), new JsonHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
          super.onSuccess(statusCode, headers, response);

          try
          {
            if (response.getBoolean("Reserved"))
            {
              mComplex.setAdminId(mCurrentUserId);
              mComplex.setName(mNameEditText.getText().toString().trim());
              mComplex.setEmail(mEmailEditText.getText().toString().trim());
              mComplex.setPhoneNumber(mPhoneNumberEditText.getText().toString().trim());

              mComplex.setLat(Double.toString(mPickedLocation != null ? mPickedLocation.latitude : 0));
              mComplex.setLong(Double.toString(mPickedLocation != null ? mPickedLocation.longitude : 0));
              mComplex.setUsername(mUsernameEditText.getText().toString().trim().toLowerCase());
              mComplex.setDescription(mDescriptionEditText.getText().toString().trim());
              mComplex.setTags(getTags());

              mComplex.setAddress(mAddress.getText().toString().trim());

              getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Bitmap profilePictureBitmap = (Bitmap) mProfilePictureImageView.getTag();
                  Bitmap coverPictureBitmap = (Bitmap) imgCover.getTag();
                  if (profilePictureBitmap != null)
                  {
                    notNulls++;
                  }
                  if (coverPictureBitmap != null)
                  {
                    notNulls++;
                  }


                  if (profilePictureBitmap != null)
                  {
                    String filePath = Images.getRealPathFromURI(getContext(), Images
                      .getImageUriFromBitmap(getContext(), profilePictureBitmap));
                    String absolutePath = null;
                    try
                    {
                      absolutePath = Images.compressJpeg(getContext(), filePath, Constants.General.APP_FOLDER_IMAGE_PATH, false);
                    } catch (IOException e)
                    {
                      e.printStackTrace();
                    }
                    uploadPicture(absolutePath, MODE_PROFILE);
                  }
                  if (coverPictureBitmap != null)
                  {
                    String filePath = Images.getRealPathFromURI(getContext(), Images
                      .getImageUriFromBitmap(getContext(), coverPictureBitmap));
                    String absolutePath = null;

                    try
                    {
                      absolutePath = Images.compressJpeg(getContext(), filePath, Constants.General.APP_FOLDER_IMAGE_PATH, false);
                    } catch (IOException e)
                    {
                      e.printStackTrace();
                    }
                    uploadPicture(absolutePath, MODE_COVER);

                  }
                  if (notNulls == 0)
                  {
                    check();
                  }

                }
              });
            } else
            {
              getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  waitDialog.dismiss();
                  if (mMenu != null)
                  {
                    mUsernameEditText.setError(getString(R.string.invalid_username));
                    mMenu.findItem(R.id.action_done).setVisible(false);
                    mMenu.findItem(R.id.menu_loader).setVisible(true);

                  }
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
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
          super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
          super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
          super.onFailure(statusCode, headers, responseString, throwable);
        }
      });

    }

  }

  @Override
  public void onValidationFailed(List<ValidationError> errors) {
    for (ValidationError error : errors)
    {
      View view = error.getView();
      String message = error.getCollatedErrorMessage(getContext());
      if (view instanceof EditText)
      {
        ((EditText) view).setError(message);
      }
    }
  }

  private List<String> getTags() {
    return Arrays.asList(mTagGroup.getTags());
  }

  @Override
  public void onLimitationExceed() {
    Utils.displayToast(getContext(), getString(R.string.toast_error_tag_limitation_exceed), Gravity.CENTER, Toast.LENGTH_SHORT);
  }
}
