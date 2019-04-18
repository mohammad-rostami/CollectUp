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
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityChangeLanguage;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.ILocation;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.receivers.LocationReceiver;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ImageButton;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

@SuppressLint ("ValidFragment")
public class FragmentPersonEdit extends BaseFragment
  implements Validator.ValidationListener, ChoosePhoto.OnDialogButtonClick, ILocation {

  @Bind (R.id.edit_text_job)
  EditText mJobEditText;
  @NotEmpty (trim = true)
  @Bind (R.id.edit_text_name)
  EditText mNameEditText;
  Profile mProfile = Logged.Models.getUserProfile();
  @Bind (R.id.image_view_picture)
  RectangleNetworkImageView mProfilePictureImageView;
  @Bind (R.id.image_view_background)
  ImageView imgCover;
  @Bind (R.id.btnEditCover)
  ImageButton btnCoverEdit;
  @Bind (R.id.spinner_gender)
  Spinner mGenderSpinner;
  @Bind (R.id.edit_text_location)
  EditText mLocation;
  @Bind (R.id.edit_text_username)
  EditText mUsername;
  @Bind (R.id.edit_text_birthday)
  TextView mBirthday;
  @Bind (R.id.text_view_languages)
  TextView mLanguages;
  @Bind (R.id.text_view_title)
  TextView profileUnderPictureName;
  @Bind (R.id.edit_text_biography)
  EditText mBiographyEditText;
  @Bind (R.id.collapsing_toolbar_layout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;
  private BottomSheetDialog mDialog;
  private Validator mValidator;
  private Menu mMenu;
  private LocationReceiver mLocationReceiver;
  private static final int MODE_COVER = 0;
  private static final int MODE_PROFILE = 1;
  private int mode;
  private SimpleDialog waitDialog;
  private View view;


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Constants.RequestCodes.CHANGE_LANGUAGE.ordinal() && resultCode == getActivity().RESULT_OK)
    {
      ArrayList<String> languages = data.getStringArrayListExtra("languages");

      mProfile.setLanguages(languages);

      mLanguages.setText(TextUtils.join(", ", languages));
    } else if (data != null)
    {
      handleResult(resultCode, data);
    }
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

  private void replaceProfilePicture(Uri uri) {
    try
    {
      mProfilePictureImageView.setTag(Images.getBitmapFromUri(getContext(), uri));
    } catch (IOException e)
    {
    }
    mProfilePictureImageView.setImageURI(uri);
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

  private void setOnLongClickToBirthday() {
    mBirthday.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {

        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_clear_birthday)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.title_clear_birthday)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.clear)
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
            mProfile.setBirthday(null);
            mBirthday.setText(null);
          }
        });
        builder.show();

        return true;
      }
    });
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_person_profile_edit, container, false);
      ButterKnife.bind(this, view);
      setHasOptionsMenu(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      mCollapsingToolbarLayout.setTitleEnabled(false);
      mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
      mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));


      initViews();

      mValidator = new Validator(this);
      mValidator.setValidationListener(this);
      return view;
    } else
    {
      return view;
    }
  }


  private void initViews() {
    profileUnderPictureName.setText(mProfile.getName());
    if (!Utils.isNullOrEmpty(mProfile.getImageAddress()))
    {
      mProfilePictureImageView.setImageUrl(Constants.General.BLOB_PROTOCOL + mProfile.getThumb(), MyApplication.getInstance().getImageLoader());
    } else
    {
      mProfilePictureImageView.setImageResource(R.drawable.ic_camera_gray);
    }
    if (!Utils.isNullOrEmpty(mProfile.getCoverPhoto()))
    {
      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mProfile.getCoverPhoto(), imgCover);
    } else
    {
      imgCover.setImageResource(R.drawable.blur_background);
    }

    mLanguages.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivityForResult(new Intent(getContext(), ActivityChangeLanguage.class).putExtra("languages", new ArrayList<>(mProfile.getLanguages())), Constants.RequestCodes.CHANGE_LANGUAGE.ordinal());
      }
    });

    if (mProfile.getLanguages() != null && mProfile.getLanguages().size() > 0)
    {
      mLanguages.setText(TextUtils.join(", ", mProfile.getLanguages()));
    }

    mUsername.setText(mProfile.getUsername());

    if (!Utils.isNullOrEmpty(mProfile.getBirthday()))
    {
      setOnLongClickToBirthday();
    }

    mBirthday.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeBirthday();
      }
    });

    mProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog = new ChoosePhoto(getActivity(), FragmentPersonEdit
          .this, false).show();
        mode = MODE_PROFILE;
      }
    });
    btnCoverEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog = new ChoosePhoto(getActivity(), FragmentPersonEdit
          .this, false).show();
        mode = MODE_COVER;

      }
    });

    mNameEditText.setText(mProfile.getName());
    mBiographyEditText.setText(mProfile.getBiography());
    mJobEditText.setText(mProfile.getJob());
    if (!Utils.isNullOrEmpty(mProfile.getBirthday()))
    {
      if (mProfile.getBirthday().contains("T"))
      {
        Date date = TimeHelper.getDateFromServerDatePattern(getContext(), mProfile.getBirthday());
        mBirthday.setText(new SimpleDateFormat(Constants.General.BIRTHDAY_PATTERN, Locale.getDefault()).format(date));
        mBirthday.setTag(new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(date));
      } else
      {
        mBirthday.setText(mProfile.getBirthday());
      }
    }

    // Attention: these numbers are based on the gender array string
    mGenderSpinner.setSelection(mProfile.getIsMan() ? 0 : 1);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_spinner_item, getResources().getStringArray(R.array.genders));
    mGenderSpinner.setAdapter(adapter);

    mNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        profileUnderPictureName.setText(s);
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.profile);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    if (mLocationReceiver == null)
    {
      mLocationReceiver = new LocationReceiver(this);
    }
    getActivity().registerReceiver(mLocationReceiver, new IntentFilter());
    super.onResume();
  }

  @Override
  public void onPause() {
    try
    {
      if (mLocationReceiver != null)
      {
        getActivity().unregisterReceiver(mLocationReceiver);
      }
    } catch (IllegalArgumentException ignored)
    {

    }
    super.onPause();
  }

  @Override
  public void onStop() {
    try
    {
      if (mLocationReceiver != null)
      {
        getActivity().unregisterReceiver(mLocationReceiver);
      }
    } catch (IllegalArgumentException ignored)
    {

    }
    super.onStop();
  }

  private void changeBirthday() {
    // Set min date to 60 years ago for the date picker.
    Calendar minDateCalendar = Calendar.getInstance();
    minDateCalendar.add(Calendar.YEAR, -60);

    Calendar userBirthdayCalendar = Calendar.getInstance();
    if (!Utils.isNullOrEmpty(mProfile.getBirthday()))
    {
      userBirthdayCalendar.setTimeInMillis(TimeHelper.utcToTimezone(getContext(), mProfile.getBirthday()));
    }

    final com.rey.material.app.DatePickerDialog datePickerDialog = new com.rey.material.app.DatePickerDialog(getContext());

    datePickerDialog.dateRange(minDateCalendar.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());

    datePickerDialog.date(userBirthdayCalendar.getTimeInMillis());

    datePickerDialog.negativeAction(R.string.cancel);
    datePickerDialog.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        datePickerDialog.dismiss();
      }
    });

    datePickerDialog.positiveAction(R.string.set);
    datePickerDialog.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        datePickerDialog.dismiss();
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePickerDialog.getYear(), datePickerDialog.getMonth(), datePickerDialog.getDay(), 0, 0, 0);
        mBirthday.setText(new SimpleDateFormat(Constants.General.BIRTHDAY_PATTERN, Locale.getDefault()).format(new Date(calendar.getTimeInMillis())));
        mBirthday.setTag(String.format("%1$d/%2$d/%3$d", datePickerDialog.getMonth() + 1, datePickerDialog.getDay(), datePickerDialog.getYear()));
        setOnLongClickToBirthday();
      }
    });

    datePickerDialog.show();
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
        Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());
        break;
      case R.id.action_done:
        mValidator.validate(true);
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
        mProfile.setCoverPhoto(null);


        break;
      case MODE_PROFILE:
        mProfilePictureImageView.setImageResource(R.drawable.ic_camera_gray);
        mProfilePictureImageView.setTag(null);
        mProfile.setImageAddress(null);

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

  int counter = 0;
  int notNulls = 0;

  private void check() {
    if (counter == notNulls)
    {

      HttpClient.put(getContext(), Constants.Server.Profile.PUT, new Gson().toJson(mProfile, Profile.class), "application/json", new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          waitDialog.dismiss();
          Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());

          Logged.Models.setUserProfile(mProfile);

          Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
          final RProfile profile = realm.where(RProfile.class).equalTo("Id", mProfile.getId()).findFirst();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              if (mProfile.getName() != null)
              {
                profile.setName(mProfile.getName());
              }
              if (mProfile.getCoverPhoto() != null)
              {
                profile.setCoverPhoto(mProfile.getCoverPhoto());
              } else
              {
                profile.setCoverPhoto("");
              }
              if (mProfile.getImageAddress() != null)
              {
                profile.setImageAddress(mProfile.getThumb());
              } else
              {
                profile.setImageAddress("");
              }
              realm.copyToRealmOrUpdate(profile);
            }
          });
          FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, mProfile);

        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          waitDialog.dismiss();
          Toast.makeText(getContext(), getString(R.string.toast_update_profile_failed), Toast.LENGTH_SHORT)
            .show();
        }
      });
    }
  }

  @Override
  public void onValidationSucceeded() {
    mMenu.findItem(R.id.action_done).setVisible(false);
    mMenu.findItem(R.id.menu_loader).setVisible(true);

    // Attention: following number is based on the gender array string
    mProfile.setIsMan(mGenderSpinner.getSelectedItemPosition() == 0);
    mProfile.setName(mNameEditText.getText().toString());
    mProfile.setJob(mJobEditText.getText().toString());
    mProfile.setBiography(mBiographyEditText.getText().toString());
    mProfile.setBirthday((String) mBirthday.getTag());

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
    waitDialog = Utils.createLoadingDialog(getContext());
    waitDialog.show();

    if (profilePictureBitmap != null)
    {
      String filePath = Images.getRealPathFromURI(getContext(), Images
        .getImageUriFromBitmap(getContext(), profilePictureBitmap));
      new Upload(getContext(), new File(filePath), UUID.randomUUID().toString(), "image/jpeg").uploadImage(new IUploadCallback() {
        @Override
        public void onFileReceived(String fileName, String uploadedPath) {
          mProfile.setImageAddress(uploadedPath);
          counter++;

          check();
        }

        @Override
        public void onFailed(int statusCode) {
          waitDialog.dismiss();
          counter++;

          check();
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {

        }
      });
    }
    if (coverPictureBitmap != null)
    {
      String filePath = Images.getRealPathFromURI(getContext(), Images
        .getImageUriFromBitmap(getContext(), coverPictureBitmap));
      new Upload(getContext(), new File(filePath), UUID.randomUUID().toString(), "image/jpeg").uploadImage(new IUploadCallback() {
        @Override
        public void onFileReceived(String fileName, String uploadedPath) {
          mProfile.setCoverPhoto(uploadedPath);
          counter++;

          check();
        }

        @Override
        public void onFailed(int statusCode) {
          waitDialog.dismiss();
          counter++;

          check();

        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {

        }
      });
    }

    if (notNulls == 0)
    {
      check();
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


  @Override
  public void LocationFound(LatLng latLng) {
    mProfile.setLat(Double.toString(latLng.latitude));
    mProfile.setLong(Double.toString(latLng.longitude));

    new AsyncTask<LatLng, Void, String>() {
      @Override
      protected void onPostExecute(String o) {
        super.onPostExecute(o);
        mLocation.setText(o);
      }

      @Override
      protected String doInBackground(LatLng... params) {
        LatLng latLng = params[0];

        return Location.getCountryAndCity(getContext(), latLng);
      }
    }.execute(latLng);
  }

  @Override
  public void onStart() {
    super.onStart();
    getActivity().startService(new Intent(getContext(), Location.class));
  }

  @Override
  public void LocationNotFound() {
    new AsyncTask<Void, Void, String>() {
      @Override
      protected void onPostExecute(String o) {
        super.onPostExecute(o);
        mLocation.setText(o);
      }

      @Override
      protected String doInBackground(Void... params) {
        LatLng latLng = new LatLng(Double.valueOf(mProfile.getLat()), Double.valueOf(mProfile.getLong()));

        return Location.getCountryAndCity(getContext(), latLng);
      }
    }.execute();
  }
}