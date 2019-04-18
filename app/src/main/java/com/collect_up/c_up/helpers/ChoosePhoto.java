/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.app.BottomSheetDialog;

/**
 * Shows a dialog to choose photo from gallery or take a new photo.
 */
public class ChoosePhoto implements View.OnClickListener {
  private final BottomSheetDialog mDialog;
  private final OnDialogButtonClick mOnDialogButtonClick;


  public ChoosePhoto(Activity activity,
                     OnDialogButtonClick onDialogButtonClick,
                     boolean hideRemovePicture) {
    mOnDialogButtonClick = onDialogButtonClick;
    mDialog = new BottomSheetDialog(activity);
    mDialog.contentView(R.layout.bottom_sheet_choosing)
      .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
      .inDuration(300)
      .cancelable(true);
    show();

    mDialog.findViewById(R.id.btnCamera).setOnClickListener(this);
    mDialog.findViewById(R.id.btnGallery).setOnClickListener(this);
    mDialog.findViewById(R.id.btnVideo).setOnClickListener(this);
    mDialog.findViewById(R.id.btnRemoveImage).setOnClickListener(this);

    // If user device has camera feature shows the take photo button
    if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
    {

      mDialog.findViewById(R.id.btnCamera).setVisibility(View.GONE);
      ((View) mDialog.findViewById(R.id.btnCamera).getParent()).setVisibility(View.GONE);
    }

    if (hideRemovePicture)
    {

      mDialog.findViewById(R.id.btnRemoveImage).setVisibility(View.GONE);
      ((View) mDialog.findViewById(R.id.btnRemoveImage).getParent()).setVisibility(View.GONE);


    }
  }


  private static void launch(Activity activity) {
  }

  public static void takePicture(final Activity activity) {
    final Activity _activity = activity;
    PermissionListener dialogPermissionListener =
      CustomPermissionDialog.Builder
        .withContext(_activity)
        .withTitle(R.string.permission_title)
        .withMessage(R.string.permission_camera)
        .withButtonText(android.R.string.ok)
        .build();
    PermissionListener basePermission = new PermissionListener() {
      @Override
      public void onPermissionGranted(PermissionGrantedResponse response) {
        launch(_activity);

      }

      @Override
      public void onPermissionDenied(PermissionDeniedResponse response) {

      }

      @Override
      public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        token.continuePermissionRequest();

      }
    };
    CompositePermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
    Dexter.withActivity(activity)
      .withPermission(Manifest.permission.CAMERA)
      .withListener(compositePermissionListener)
      .check();


  }

  public BottomSheetDialog show() {
    mDialog.show();
    return mDialog;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.btnCamera:
        mOnDialogButtonClick.onDialogTakePhoto();
        mDialog.dismiss();
        break;
      case R.id.btnRemoveImage:
        mOnDialogButtonClick.onDialogRemovePhoto();
        mDialog.dismiss();
        break;
      case R.id.btnGallery:
        mOnDialogButtonClick.onDialogFromGallery();
        mDialog.dismiss();
        break;
      case R.id.btnVideo:
        mOnDialogButtonClick.onDialogFromVideo();
        mDialog.dismiss();
        break;
    }
  }


  public interface OnDialogButtonClick {
    void onDialogTakePhoto();

    void onDialogRemovePhoto();

    void onDialogFromGallery();

    void onDialogFromVideo();
  }
}
