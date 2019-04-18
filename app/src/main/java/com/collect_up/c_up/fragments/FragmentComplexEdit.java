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

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
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
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ImageButton;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import me.gujun.android.taggroup.TagGroup;

@SuppressLint ("ValidFragment")
public class FragmentComplexEdit extends BaseFragment implements GoogleMap.OnMapClickListener,
  Validator.ValidationListener,
  ChoosePhoto.OnDialogButtonClick, TagGroup.OnTagLimitationExceedListener {

  private EditText mAddressEditText;
  private Complex mComplex;
  private EditText mDescriptionEditText;
  private BottomSheetDialog mDialog;
  private EditText mEmailEditText;
  private GoogleMap mGoogleMap;
  @NotEmpty (trim = true)
  private EditText mNameEditText;
  private EditText mPhoneNumberEditText;
  private LatLng mPickedLocation;
  private RectangleNetworkImageView mProfilePictureImageView;
  private TagGroup mTagGroup;
  private Validator mValidator;
  private Menu mMenu;
  private TextView nameUnderPicture;
  private Button btnUpdate;
  private ImageView imgCover;
  private ImageButton btnCoverEdit;
  public LatLng latLng;
  private boolean isProfileSet;
  public boolean forcePost;
  private static final int MODE_COVER = 0;
  private static final int MODE_PROFILE = 1;
  private int mode;
  private SimpleDialog waitDialog;
  private SimpleDialog builder;
  private View view;

  public FragmentComplexEdit(Complex mComplex) {
    this.mComplex = mComplex;
  }

  public FragmentComplexEdit() {
  }

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
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickedLocation.latitude, pickedLocation.longitude), 5));
      }
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

  private void uploadPicture(String path, final int mode) {
    if (mMenu != null)
    {
      mMenu.findItem(R.id.action_done).setVisible(false);
      mMenu.findItem(R.id.menu_loader).setVisible(true);

    }


    String filePath = path;
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

          if (mMenu != null)
          {
            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

          }
          check();

        }

        @Override
        public void onFailed(int statusCode) {
          waitDialog.dismiss();
          counter++;
          check();
          if (mMenu != null)
          {
            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

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

  int counter = 0;
  int notNulls = 0;

  private void check() {
    if (counter == notNulls)
    {


      HttpClient.put(getContext(), Constants.Server.Complex.PUT, new Gson().toJson(mComplex, Complex.class), "application/json", new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());

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
          waitDialog.dismiss();

          mMenu.findItem(R.id.action_done).setVisible(true);
          mMenu.findItem(R.id.menu_loader).setVisible(false);

          Toast.makeText(getContext(), getString(R.string.toast_edit_complex_failed), Toast.LENGTH_SHORT)
            .show();
        }
      });
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
    mProfilePictureImageView.setImageDrawable(null);
    mProfilePictureImageView.setImageURI(uri);
    isProfileSet = true;

  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.complex);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      setHasOptionsMenu(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();

      view = inflater.inflate(R.layout.fragment_complex_edit, container, false);
      CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
      collapsingToolbarLayout.setTitleEnabled(false);
      collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
      collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));


      mNameEditText = (EditText) view.findViewById(R.id.edit_text_name);
      mEmailEditText = (EditText) view.findViewById(R.id.edit_text_email);
      mAddressEditText = (EditText) view.findViewById(R.id.edit_text_address);
      mPhoneNumberEditText = (EditText) view.findViewById(R.id.edit_text_phone_number);
      mDescriptionEditText = (EditText) view.findViewById(R.id.edit_text_description);
      mProfilePictureImageView = (RectangleNetworkImageView) view.findViewById(R.id.image_view_picture);
      mTagGroup = (TagGroup) view.findViewById(R.id.tag_group);
      nameUnderPicture = (TextView) view.findViewById(R.id.text_view_title);
      imgCover = (ImageView) view.findViewById(R.id.image_view_background);
      btnCoverEdit = (ImageButton) view.findViewById(R.id.btnEditCover);

      btnUpdate = (Button) view.findViewById(R.id.btnUpdate);
      btnUpdate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), ActivityPickLocation.class);
          intent.putExtra("location", latLng);
          intent.putExtra("return", true);
          startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
        }
      });
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
    ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view)).getMapAsync(new OnMapReadyCallback() {

      @Override
      public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        latLng = new LatLng(Double.valueOf(mComplex.getLat()), Double.valueOf(mComplex.getLong()));
        if (latLng.latitude == 0 && latLng.longitude == 0)
        {
        } else
        {
          mGoogleMap.addMarker(new MarkerOptions().position(latLng)
            .title(getString(R.string.placed_here)));
          mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));

          mGoogleMap.setOnMapClickListener(FragmentComplexEdit.this);
        }
        mPickedLocation = latLng;

      }
    });

    // Set title to the text view
    nameUnderPicture.setText(mComplex.getName());

    mNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        nameUnderPicture.setText(s);
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    mTagGroup.setTags(new ArrayList<String>());

    // TagGroup should be in this positions to prevent from showing toast for the first time.
    mTagGroup.setLimitation(Constants.General.TAG_LIMITATION);
    if (mComplex.getTags() != null)
    {
      mTagGroup.setTags(mComplex.getTags());
    }
    mTagGroup.setOnLimitationExceedListener(this);

    mProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog = new ChoosePhoto(getActivity(), FragmentComplexEdit.this, false)
          .show();
        mode = MODE_PROFILE;

      }
    });
    btnCoverEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog = new ChoosePhoto(getActivity(), FragmentComplexEdit.this, false).show();
        mode = MODE_COVER;

      }
    });

    mNameEditText.setText(mComplex.getName());
    mEmailEditText.setText(mComplex.getEmail());
    mAddressEditText.setText(mComplex.getAddress());
    mPhoneNumberEditText.setText(mComplex.getPhoneNumber());
    mDescriptionEditText.setText(mComplex.getDescription());
    if (!Utils.isNullOrEmpty(mComplex.getCoverPhoto()))
    {
      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mComplex.getCoverPhoto(), imgCover);
    } else
    {
      imgCover.setImageResource(R.drawable.blur_background);
    }
    if (!Utils.isNullOrEmpty(mComplex.getImageAddress()))
    {
      isProfileSet = true;
      mProfilePictureImageView.setImageUrl(Constants.General.BLOB_PROTOCOL + mComplex.getThumb(), MyApplication.getInstance().getImageLoader());

      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mComplex.getThumb(), mProfilePictureImageView);

    } else
    {
      mProfilePictureImageView.setImageResource(R.drawable.ic_camera_gray);

    }
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
        //  onBackPressed();
        break;
      case R.id.action_done:
        if (!isProfileSet)
        {
          Toast.makeText(getContext(), getString(R.string.validate_image), Toast.LENGTH_SHORT).show();
        } else if (mTagGroup.getTags().length < 2)
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
  public void onMapClick(final LatLng latLng) {
  }

  @Override
  public void onValidationSucceeded() {
    mMenu.findItem(R.id.action_done).setVisible(false);
    mMenu.findItem(R.id.menu_loader).setVisible(true);


    if (mPickedLocation.latitude == 0 && mPickedLocation.longitude == 0 && !forcePost)
    {
      builder = new com.rey.material.app.SimpleDialog(getContext());
      builder.message(R.string.alert_message_pick_location)
        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
        .title(R.string.alert_title_pick_location)
        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .positiveAction(R.string.alert_positive_pick_location)
        .negativeAction(R.string.alert_negetive_pick_location)
        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .cancelable(false);
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
      mComplex.setName(mNameEditText.getText().toString().trim());
      mComplex.setEmail(mEmailEditText.getText().toString().trim());
      mComplex.setPhoneNumber(mPhoneNumberEditText.getText().toString().trim());
      mComplex.setAddress(mAddressEditText.getText().toString().trim());
      if (mPickedLocation != null)
      {
        mComplex.setLat(Double.toString(mPickedLocation.latitude));
        mComplex.setLong(Double.toString(mPickedLocation.longitude));
      }
      mComplex.setDescription(mDescriptionEditText.getText().toString().trim());
      String[] tags = mTagGroup.getTags();
      mComplex.setTags(Arrays.asList(tags));
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
      if (builder != null)
      {
        builder.dismiss();
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
  public void onLimitationExceed() {
    Utils.displayToast(getContext(), getString(R.string.toast_error_tag_limitation_exceed), Gravity.CENTER, Toast.LENGTH_SHORT);
  }
}
