/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.activities;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;

import butterknife.Bind;

public class ActivityImageViewer extends BaseActivity {

  @Bind (R.id.image_view_picture)
  ImageView mPicture;
  @Bind (R.id.progress_bar_loading)
  ProgressView mLoading;
  @Bind (R.id.toolbar)
  Toolbar mToolbar;

  private String mAddress;
  private String mFilePathInLocal;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display_image);

    setSupportActionBar(mToolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
    }

    Bundle args = getIntent().getExtras();
    if (args != null)
    {
      String address = args.getString("address");
      String name = args.getString("title");

      if (address != null)
      {
        String[] spllited = address.split("/");
        String fileName = spllited[spllited.length - 1];

        mFilePathInLocal = Constants.General.APP_FOLDER_IMAGE_PATH + fileName;
        mAddress = address;

        if (actionBar != null)
        {
          actionBar.setTitle(name);
        }

        File localFile = new File(mFilePathInLocal);

        if (!localFile.exists() && !Utils.isNullOrEmpty(address))
        {
          Picasso.with(this).load(address).noPlaceholder().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
              mLoading.setVisibility(View.GONE);
              mPicture.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
              Toast.makeText(ActivityImageViewer.this, R.string.toast_error_unable_to_load_image, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
          });
        } else if (localFile.exists())
        {
          Picasso.with(this).load(localFile).noPlaceholder().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
              mLoading.setVisibility(View.GONE);
              mPicture.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
              Toast.makeText(ActivityImageViewer.this, R.string.toast_error_unable_to_load_image, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
          });
        }
      }

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_display_image, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId())
    {
      case android.R.id.home:
        onBackPressed();
        break;
      case R.id.action_save:
        PermissionListener dialogPermissionListener =
          CustomPermissionDialog.Builder
            .withContext(this)
            .withTitle(R.string.permission_title)
            .withMessage(R.string.permission_storage)
            .withButtonText(android.R.string.ok)
            .build();
        PermissionListener basePermission = new PermissionListener() {
          @Override
          public void onPermissionGranted(PermissionGrantedResponse response) {

            DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(mAddress))
              .setRetryPolicy(new DefaultRetryPolicy())
              .setDestinationURI(Uri.fromFile(new File(mFilePathInLocal)))
              .setPriority(DownloadRequest.Priority.HIGH)
              .setDownloadListener(new DownloadStatusListener() {
                @Override
                public void onDownloadComplete(int id) {
                  Toast.makeText(ActivityImageViewer.this, R.string.toast_image_saved_to_gallery, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                  Toast.makeText(ActivityImageViewer.this, R.string.toast_error_image_not_downloaded, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {

                }
              });
            ThinDownloadManager downloadManager = new ThinDownloadManager();
            downloadManager.add(downloadRequest);
          }

          @Override
          public void onPermissionDenied(PermissionDeniedResponse response) {

          }

          @Override
          public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();

          }
        };
        PermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
        Dexter.withActivity(ActivityImageViewer.this)
          .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
          .withListener(compositePermissionListener)
          .check();


        break;
    }

    return true;
  }


  @Override
  public void onMenuNotificationsClick() {

  }

  @Override
  public void onMenuTimelineClick() {

  }

  @Override
  public void onMenuShopClick() {

  }

  @Override
  public void onConversationClick() {

  }
}
