/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentProductProfile;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.rey.material.widget.ProgressView;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.meness.timelinepostcontainer.TimelinePostContainer;
import io.github.meness.timelinepostcontainer.Type;

/**
 * Created by Collect-up  on 11/2/2015.
 */
public class CustomSlider extends BaseSliderView {
  private final String mType;
  private final String mImagePath;
  private final String mVideoPath;
  private final String mPdfPath;
  private final int mPage;
  private List<String> imagesPath;

  public CustomSlider(Activity context, String type, String imagePath, String videoPath, String pdfPath, int page) {
    super(context);
    mType = type;
    mImagePath = imagePath;
    mVideoPath = videoPath;
    mPdfPath = pdfPath;
    mPage = page;
  }

  public void setImageArray(List<String> images) {
    imagesPath = images;
  }

  @Override
  public View getView() {
    if (mType.equals("image"))
    {
      View v = LayoutInflater.from(getContext()).inflate(R.layout.item_slider_video_product, null);
      final TimelinePostContainer postContainer = (TimelinePostContainer) v.findViewById(R.id.timelinePostContainer);

      postContainer.setImagePath(Constants.General.BLOB_PROTOCOL + mImagePath);
      postContainer.findViewById(R.id.image_view_video_camera_icon).setVisibility(View.GONE);

      postContainer.setImageLoader(MyApplication.getInstance().getImageLoader()).build(Type.IMAGE);
      ImageView target = (ImageView) v.findViewById(R.id.image_view_empty);
      bindEventAndShow(v, target);
      ((ImageView) postContainer.getChildAt(0)).setScaleType(ImageView.ScaleType.CENTER_CROP);
      postContainer.getChildAt(0).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          ArrayList<Uri> images = new ArrayList<Uri>();
          for (int i = 0; i < imagesPath.size(); i++)
          {
            images.add(Uri.parse(Constants.General.BLOB_PROTOCOL + imagesPath.get(i)));
          }
          Utils.displayImageInternalGallery((Activity) mContext, images, postContainer.getChildAt(0), mPage);


        }
      });
      return v;
    } else if (mType.equals("video"))
    {
      View v = LayoutInflater.from(getContext()).inflate(R.layout.item_slider_video_product, null);
      TimelinePostContainer postContainer = (TimelinePostContainer) v.findViewById(R.id.timelinePostContainer);

      postContainer.setVideoPath(Constants.General.BLOB_PROTOCOL + mVideoPath);
      postContainer.setImagePath(Constants.General.BLOB_PROTOCOL + mImagePath);
      postContainer.findViewById(R.id.image_view_video_camera_icon).setVisibility(View.VISIBLE);

      postContainer.setImageLoader(MyApplication.getInstance().getImageLoader()).build(Type.VIDEO);

      ImageView target = (ImageView) v.findViewById(R.id.image_view_empty);
      bindEventAndShow(v, target);
      return v;
    } else
    {
      View v = LayoutInflater.from(getContext()).inflate(R.layout.item_slider_catalogue_product, null);
      ImageView target = (ImageView) v.findViewById(R.id.image_view_catalogue);
      final ProgressView progressBar = (ProgressView) v.findViewById(R.id.progress);

      target.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
          builder.message(R.string.want_to_download_catalogue)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.download_confirmation_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.download)
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
              builder.dismiss();
              String[] splitted = mPdfPath.split("/");
              final File downloadedPathFile = new File(Constants.General.APP_FOLDER_FILE_PATH + splitted[splitted.length - 1]);
              DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(Constants.General.BLOB_PROTOCOL + mPdfPath))
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(Uri.fromFile(downloadedPathFile))
                .setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadListener(new DownloadStatusListener() {
                  @Override
                  public void onDownloadComplete(int id) {
                    progressBar.setVisibility(View.GONE);
                    progressBar.setProgress(0);

                    if (FragmentProductProfile.isRunning)
                    {
                      final com.rey.material.app.SimpleDialog openDialog = new com.rey.material.app.SimpleDialog(getContext());
                      openDialog.message(R.string.want_to_open_catalogue)
                        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                        .title(R.string.download_completed_title)
                        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .positiveAction(R.string.open)
                        .negativeAction(R.string.dismiss)
                        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .setCancelable(true);

                      openDialog.negativeActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          openDialog.dismiss();

                        }
                      });
                      openDialog.positiveActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          Utils.displayPdf(getContext(), Uri.fromFile(downloadedPathFile));
                          openDialog.dismiss();

                        }
                      });
                      openDialog.show();
                    }
                  }

                  @Override
                  public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                  }

                  @Override
                  public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                  }
                });

              ThinDownloadManager downloadManager = new ThinDownloadManager();
              downloadManager.add(downloadRequest);
            }
          });
          builder.show();
        }
      });

      bindEventAndShow(v, target);
      return v;
    }

  }
}
